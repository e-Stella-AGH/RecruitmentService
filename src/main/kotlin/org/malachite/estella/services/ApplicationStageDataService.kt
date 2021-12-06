package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.interview.api.NotesFilePayload
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationStageDataService(
    @Autowired private val applicationStageRepository: ApplicationStageRepository,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val taskStageService: TaskStageService,
    @Autowired private val interviewService: InterviewService,
    @Autowired private val noteService: NoteService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val mailService: MailService
) : EStellaService<ApplicationStageData>() {
    override val throwable: Exception = ApplicationNotFoundException()

    fun getAllApplicationStageData() = applicationStageRepository.findAll()

    fun createApplicationStageData(
        application: Application,
        recruitmentStage: RecruitmentStage,
        devs: MutableList<String>
    ): ApplicationStageData {
        val applicationStage = ApplicationStageData(
            null,
            recruitmentStage,
            application,
            null,
            null,
            setOf(),
            devs.toMutableSet()
        ).let { applicationStageRepository.save(it) }
        return getTaskStageAndInterview(recruitmentStage, applicationStage).let { pair ->
            pair.first
                ?.also { taskStage -> setHostsForTaskStage(taskStage, devs.toSet()) }
                .let {
                    applicationStage.copy(
                        tasksStage = it,
                        interview = pair.second
                    )
                }
        }.let { applicationStageRepository.save(it) }
    }

    private fun getTaskStageAndInterview(
        recruitmentStage: RecruitmentStage,
        applicationStage: ApplicationStageData,
    ): Pair<TaskStage?, Interview?> =
        when (recruitmentStage.type) {
            StageType.TASK -> {
                val taskStage = taskStageService.createTaskStage(applicationStage, null)
                Pair(taskStage, null)
            }
            StageType.TECHNICAL_INTERVIEW -> {
                val interview = interviewService.createInterview(applicationStage)
                val taskStage = taskStageService.createTaskStage(applicationStage, interview)
                Pair(taskStage, interview)
            }
            StageType.HR_INTERVIEW -> {
                val interview = interviewService.createInterview(applicationStage)
                Pair(null, interview)
            }
            else -> Pair(null, null)
        }


    fun setNotesToInterview(id: UUID, password: String?, notes: Set<NotesFilePayload>) =
        interviewService.getInterview(id).applicationStage
            .let { setNotesToApplicationStage(it, password, notes) }

    fun setNotesToTaskStage(id: UUID, password: String?, notes: Set<NotesFilePayload>) =
        taskStageService.getTaskStage(id).applicationStage.let { setNotesToApplicationStage(it, password, notes) }

    fun setNotesToApplied(application: Application, password: String?, notes: Set<NotesFilePayload>) =
        application.applicationStages.first().let { setNotesToApplicationStage(it, password, notes) }

    private fun setNotesToApplicationStage(
        applicationStage: ApplicationStageData,
        password: String?,
        notes: Set<NotesFilePayload>
    ) = assertAccessApplicationStageData(applicationStage, password)
        .let { applicationStage.notes.plus(noteService.updateNotes(notes)) }
        .let { applicationStageRepository.save(applicationStage.copy(notes = it)) }


    fun getNotesByApplication(application: Application, password: String?): List<Note> =
        application.applicationStages.first()
            .let { getNotesByApplicationStage(it, password) }


    fun getNotesByInterviewId(id: UUID, password: String?): List<Note> =
        interviewService.getInterview(id).applicationStage
            .let { getNotesByApplicationStage(it, password) }


    fun getNotesByTaskId(id: UUID, password: String?): List<Note> =
        taskStageService.getTaskStage(id).applicationStage
            .let { getNotesByApplicationStage(it, password) }

    private fun getNotesByApplicationStage(applicationStage: ApplicationStageData, password: String?): List<Note> =
        assertAccessApplicationStageData(applicationStage, password)
            .let { applicationStage.application!!.applicationStages.flatMap { it.notes }.distinct() }

    fun getNotesByTaskIdWithTask(id: UUID, password: String?): TasksNotes =
        taskStageService.getTaskStage(id)
            .let { getNotesWithTaskResults(it, password) }

    private fun getNotesWithTaskResults(taskStage: TaskStage, password: String?): TasksNotes =
        taskStage
            .also { assertAccessApplicationStageData(it.applicationStage, password) }
            .let { Pair(it.tasksResult, it.applicationStage.notes.toList()) }


    private fun assertAccessApplicationStageData(applicationStage: ApplicationStageData, password: String?): Unit =
        when {
            password != null && !canDevUpdate(applicationStage, password) -> throw UnauthenticatedException()
            password == null && !canHrUpdate(applicationStage) -> throw UnauthenticatedException()
            else -> Unit
        }

    private fun canDevUpdate(applicationStage: ApplicationStageData, password: String): Boolean =
        recruitmentProcessService
            .getProcessFromStage(applicationStage)
            .offer.creator.organization.let {
                securityService.compareOrganizationWithPassword(it, password)
            }

    private fun canHrUpdate(applicationStage: ApplicationStageData): Boolean =
        recruitmentProcessService
            .getProcessFromStage(applicationStage)
            .offer.creator.let { hrPartner ->
                securityService.getUserDetailsFromContext()
                    ?.let { it.user == hrPartner.user }
                    ?: false
            }

    fun setHostsForInterview(interviewId: UUID, hostsMails: MutableSet<String>) {
        val interview = interviewService.getInterview(interviewId)
        val savedApplicationStage =
            applicationStageRepository.save(interview.applicationStage.copy(hosts = hostsMails))
        val offer = recruitmentProcessService.getProcessFromStage(savedApplicationStage).offer
        hostsMails.forEach { mail ->
            mailService.sendInterviewHostInvitationMail(
                offer,
                interview,
                savedApplicationStage.application,
                mail
            )
        }
    }

    fun setHostsForTaskStage(taskStage: TaskStage, hostsMails: Set<String>): ApplicationStageData {
        val savedApplicationStage =
            applicationStageRepository.save(taskStage.applicationStage.copy(hosts = hostsMails.toMutableSet()))
        val offer = recruitmentProcessService.getProcessFromStage(savedApplicationStage).offer
        hostsMails.forEach { mailService.sendTaskAssignmentRequest(it, taskStage, offer) }
        return savedApplicationStage
    }
}

typealias TasksNotes = Pair<Set<TaskResult>, List<Note>>
package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.interview.api.NotesFilePayload
import org.malachite.estella.task.domain.TaskStageNotFoundException
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

    fun createApplicationStageData(application: Application, recruitmentStage: RecruitmentStage, devs: MutableList<String>?): ApplicationStageData {
        val applicationStage = ApplicationStageData(
            null,
            recruitmentStage,
            application,
            null,
            null,
            setOf()
        ).let { applicationStageRepository.save(it) }
        return getTaskStageAndInterview(recruitmentStage, applicationStage).let {
            it.first?.let { taskStage ->
                taskStageService.setDevs(taskStage.id!!, devs?: mutableListOf())
                devs?.forEach { mailService.sendTaskAssignmentRequest(it, taskStage, recruitmentProcessService.getProcessFromStage(applicationStage).offer) }
            }
            applicationStage.copy(
                    tasksStage = it.first,
                    interview = it.second)
        }
        .let { applicationStageRepository.save(it) }
    }

    private fun getTaskStageAndInterview(recruitmentStage: RecruitmentStage, applicationStage: ApplicationStageData) =
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
            .let { applicationStage.application.applicationStages.flatMap { it.notes } }

    fun getNotesByTaskIdWithTask(id: UUID, password: String?): TasksNotes =
        taskStageService.getTaskStage(id).applicationStage
            .let { getNotesWithTaskResults(it, password) }

    private fun getNotesWithTaskResults(applicationStage: ApplicationStageData, password: String?): TasksNotes =
        applicationStage
            .also { assertAccessApplicationStageData(it, password) }
            .let { Pair(it.tasksStage!!.tasksResult, it.notes.toList()) }


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


    fun getCurrentStageType(applicationStageId: Int): StageType {
        val applicationStage = applicationStageRepository.findById(applicationStageId)
        if (applicationStage.isEmpty) throw TaskStageNotFoundException()
        val application = applicationStage.get().application
        val recruitmentProcess = recruitmentProcessService.getProcessFromStage(applicationStage.get())

        val recruitmentProcessStages = recruitmentProcess
                .stages
                .sortedBy { it.id }

        val applicationRecruitmentStages = application.applicationStages.map { it.stage }.sortedBy { it.id }

        val indexOfRecruitmentStage = recruitmentProcessStages.indexOf(applicationRecruitmentStages.last())
        return recruitmentProcessStages[indexOfRecruitmentStage].type
    }
}

typealias TasksNotes = Pair<List<TaskResult>, List<Note>>
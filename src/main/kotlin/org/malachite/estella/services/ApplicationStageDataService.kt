package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
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
    @Autowired private val securityService: SecurityService
) : EStellaService<ApplicationStageData>() {
    override val throwable: Exception = ApplicationNotFoundException()

    fun createApplicationStageData(application: Application, recruitmentStage: RecruitmentStage): ApplicationStageData {
        val applicationStage = ApplicationStageData(
            null,
            recruitmentStage,
            application,
            null,
            null,
            setOf()
        ).let { applicationStageRepository.save(it) }
        val taskAndInterview = getTaskStageAndInterview(recruitmentStage, applicationStage)
        return applicationStage.copy(
            tasksStage = taskAndInterview.first,
            interview = taskAndInterview.second
        ).let { applicationStageRepository.save(it) }
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


    fun setNotesToInterview(id: UUID, password: String, notes: Set<NotesFilePayload>) {
        if (!canDevUpdate(id, password)) throw UnauthenticatedException()
        val interview = interviewService.getInterview(id)
        val savedNotes = interview.applicationStage.notes.plus(noteService.updateNotes(notes))
        applicationStageRepository.save(interview.applicationStage.copy(notes = savedNotes))
    }

    private fun canDevUpdate(id: UUID?, password: String): Boolean =
        id?.let {
            val organization = recruitmentProcessService
                .getProcessFromStage(interviewService.getInterview(it).applicationStage)
                .offer.creator.organization
            securityService.compareOrganizationWithPassword(organization, password)
        } ?: false
}
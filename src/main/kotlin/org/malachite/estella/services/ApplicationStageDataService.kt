package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApplicationStageDataService(
    @Autowired private val applicationStageRepository: ApplicationStageRepository,
    @Autowired private val taskStageService: TaskStageService,
    @Autowired private val interviewService: InterviewService,
) : EStellaService<ApplicationStageData>() {
    override val throwable: Exception = ApplicationNotFoundException()

    fun createApplicationStageData(application: Application, recruitmentStage: RecruitmentStage): ApplicationStageData {
        val applicationStage = ApplicationStageData(
            null,
            recruitmentStage,
            application,
            null,
            null
        ).let { applicationStageRepository.save(it) }
        val taskAndInterview = getTasStageAndInterview(recruitmentStage, applicationStage)
        return applicationStage.copy(
            tasksStage = taskAndInterview.first,
            interview = taskAndInterview.second
        ).let { applicationStageRepository.save(it) }
    }

    private fun getTasStageAndInterview(recruitmentStage: RecruitmentStage, applicationStage: ApplicationStageData) =
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

}
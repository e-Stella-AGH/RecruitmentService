package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.task.domain.TaskStageNotFoundException
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TaskStageService(
    @Autowired private val taskStageRepository: TaskStageRepository,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService
) : EStellaService<TaskStage>() {
    override val throwable: Exception = TaskStageNotFoundException()


    fun createTaskStage(applicationStage: ApplicationStageData, interview: Interview?): TaskStage =
        TaskStage(null, setOf(), applicationStage)
            .let { taskStageRepository.save(it) }

}
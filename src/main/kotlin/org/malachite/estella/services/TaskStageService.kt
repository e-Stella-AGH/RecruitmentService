package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.task.domain.TaskResultRepository
import org.malachite.estella.task.domain.TaskStageNotFoundException
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaskStageService(
        @Autowired private val taskStageRepository: TaskStageRepository,
        @Autowired private val taskResultRepository: TaskResultRepository
) : EStellaService<TaskStage>() {
    override val throwable: Exception = TaskStageNotFoundException()


    fun createTaskStage(applicationStage: ApplicationStageData, interview: Interview?): TaskStage =
            TaskStage(null, listOf(), applicationStage)
                    .let { taskStageRepository.save(it) }

    fun getTaskStage(taskStageId: UUID) = withExceptionThrower { taskStageRepository.findById(taskStageId).get() }

    fun getTaskStage(taskStageId: String) = withExceptionThrower { getTaskStage(UUID.fromString(taskStageId)) }

    fun addResult(result: TaskResult) {
        val savedResult = taskResultRepository.save(result)
        val taskStage = savedResult.taskStage
        val newTaskResults = taskStage.tasksResult.plus(savedResult)
        taskStageRepository.save(taskStage.copy(tasksResult = newTaskResults))
    }

}
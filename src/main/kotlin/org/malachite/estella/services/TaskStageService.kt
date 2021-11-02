package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskResultRepository
import org.malachite.estella.task.domain.TaskStageNotFoundException
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaskStageService(
        @Autowired private val taskStageRepository: TaskStageRepository,
        @Autowired private val taskResultRepository: TaskResultRepository,
        @Autowired private val taskRepository: TaskRepository,
        @Autowired private val interviewService: InterviewService,
        @Autowired @Lazy private val applicationStageDataService: ApplicationStageDataService,
        @Autowired private val securityService: SecurityService
) : EStellaService<TaskStage>() {
    override val throwable: Exception = TaskStageNotFoundException()

    fun createTaskStage(applicationStage: ApplicationStageData, interview: Interview?): TaskStage =
            TaskStage(null, listOf(), applicationStage, mutableListOf())
                    .let { taskStageRepository.save(it) }

    fun getAll() = taskStageRepository.findAll()

    fun getTaskStage(taskStageId: UUID) = withExceptionThrower { taskStageRepository.findById(taskStageId).get() }

    fun getTaskStage(taskStageId: String) = withExceptionThrower { getTaskStage(UUID.fromString(taskStageId)) }

    fun addResult(result: TaskResult) {
        val resultToSave = taskResultRepository.findAll().firstOrNull { it.task.id == result.task.id && it.taskStage.id == result.taskStage.id }
                ?.copy(results = result.results,
                        code = result.code,
                        startTime = result.startTime,
                        endTime = result.endTime,
                        task = result.task,
                        taskStage = result.taskStage
                )
                ?: result
        val savedResult = taskResultRepository.save(resultToSave)
        val taskStage = savedResult.taskStage
        val newTaskResults = taskStage.tasksResult.filter { it.task.id != resultToSave.task.id }.plus(savedResult)
        taskStageRepository.save(taskStage.copy(tasksResult = newTaskResults))
    }

    fun setDevs(id: UUID, devs: MutableList<String>) =
        getTaskStage(id).let { taskStageRepository.save(it.copy(devs = devs)) }

    fun setTasks(taskStageUuid: String, tasksIds: Set<Int>, password: String) {
        if (securityService.getTaskStageFromPassword(password)?.let {
                    it.id.toString() != taskStageUuid ||
                    !isTaskStageCurrentStage(it)
        } == true)
            throw UnauthenticatedException()
        tasksIds.map { taskRepository.findById(it).orElse(null) }.let {
            deleteRemovedTaskResults(taskStageUuid, tasksIds)
            addMissingTaskResults(taskStageUuid, it)
        }
    }

    fun setTasksByInterviewUuid(interviewUuid: String, tasksIds: Set<Int>, password: String) =
        interviewService
                .getInterview(UUID.fromString(interviewUuid))
                .applicationStage
                .tasksStage
                .let { taskStage ->
                    setTasks(taskStage!!.id.toString(), tasksIds, password)
                }

    private fun isTaskStageCurrentStage(taskStage: TaskStage): Boolean =
        taskStage.applicationStage.id?.let { applicationStageDataService.getCurrentStageType(it) } ==
                taskStage.applicationStage.stage.type


    private fun deleteRemovedTaskResults(taskStageUuid: String, tasksIds: Set<Int>) =
            getTaskStage(taskStageUuid).tasksResult.filterNot { tasksIds.contains(it.id) }
                    .also {
                        val taskStage = getTaskStage(taskStageUuid)
                        taskStageRepository.save(taskStage.copy(tasksResult = taskStage.tasksResult.minus(it.toSet()))) }
                    .forEach{ taskResultRepository.delete(it) }

    private fun addMissingTaskResults(taskStageUuid: String, tasks: List<Task>) {
        getTaskStage(taskStageUuid).let { taskStage ->
            {
                tasks.filterNot { task -> taskStage.tasksResult.map { it.task.id }.contains(task.id) }
                        .map {
                            taskResultRepository.save(TaskResult(null, null, null, null, null, it, taskStage))
                        }.also {
                            val stage = getTaskStage(taskStageUuid)
                            taskStageRepository.save(stage.copy(tasksResult = stage.tasksResult.plus(it)))
                        }
            }
        }
    }


}
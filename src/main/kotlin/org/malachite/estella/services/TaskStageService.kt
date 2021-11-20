package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.toTaskDto
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskResultRepository
import org.malachite.estella.task.domain.TaskStageNotFoundException
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TaskStageService(
        @Autowired private val taskStageRepository: TaskStageRepository,
        @Autowired private val taskResultRepository: TaskResultRepository,
        @Autowired private val taskRepository: TaskRepository,
        @Autowired private val interviewService: InterviewService,
        @Autowired private val recruitmentProcessService: RecruitmentProcessService,
        @Autowired private val organizationService: OrganizationService,
        @Autowired private val mailService: MailService,
        @Autowired private val securityService: SecurityService
) : EStellaService<TaskStage>() {
    override val throwable: Exception = TaskStageNotFoundException()

    fun createTaskStage(applicationStage: ApplicationStageData, interview: Interview?): TaskStage =
            TaskStage(null, listOf(), applicationStage, mutableListOf())
                    .let { taskStageRepository.save(it) }

    fun getAll() = taskStageRepository.findAll()

    fun getTaskStage(taskStageId: UUID) = withExceptionThrower { taskStageRepository.findById(taskStageId).get() }

    fun getTaskStage(taskStageId: String) = withExceptionThrower { getTaskStage(UUID.fromString(taskStageId)) }


    fun assertDevPasswordCorrect(organizationUuid: String, password: String) =
            organizationService.getOrganization(organizationUuid).let {
                if (!securityService.compareOrganizationWithPassword(it, password))
                    throw UnauthenticatedException()
                this
            }

    fun checkDevPasswordFromInterviewUuid(interviewUuid: String, password: String) =
            interviewService.getInterview(UUID.fromString(interviewUuid)).applicationStage.tasksStage
                    .let { getOrganizationUuidFromTaskStage(it!!) }
                    .let { organizationService.getOrganization(it) }
                    .let {
                        assertDevPasswordCorrect(it.id.toString(), password)
                    }

    fun checkDevPasswordFromTaskStage(taskStageUuid: String, password: String) =
            getOrganizationUuidFromTaskStage(getTaskStage(taskStageUuid))
                    .let { organizationService.getOrganization(it) }
                    .let {
                        assertDevPasswordCorrect(it.id.toString(), password)
                    }


    fun getTasksByOrganizationUuid(organizationUuid: UUID) =
            organizationService.getOrganization(organizationUuid)
                    .tasks
                    .map { it.toTaskDto() }

    fun getTasksByTasksStage(tasksStageId: String, password: String): List<TaskDto> {
        val taskStage = getTaskStage(tasksStageId)
        val organizationUuid = getOrganizationUuidFromTaskStage(taskStage)
        assertDevPasswordCorrect(organizationUuid, password)
        return taskStage.tasksResult.map { it.task.toTaskDto() }
    }

    fun getTasksByInterview(interviewId: String): List<TaskDto> =
            interviewService.getInterview(UUID.fromString(interviewId)).applicationStage.tasksStage
                    ?.let {
                        it.tasksResult.map { it.task.toTaskDto() }
                    }
                    ?: listOf()

    private fun getOrganizationUuidFromTaskStage(taskStage: TaskStage) =
            recruitmentProcessService.getProcessFromStage(taskStage.applicationStage).offer.creator.organization.id.toString()

    fun getTasksByDev(devMail: String, password: String): List<TaskDto> {
        val decodedMail = String(Base64.getDecoder().decode(devMail))
        val tasksStages = getAll().filter { it.devs.contains(decodedMail) }
        tasksStages.forEach{ assertDevPasswordCorrect(getOrganizationUuidFromTaskStage(it), password) }
        return tasksStages.map { it.tasksResult }.flatMap { it.map { it.task.toTaskDto() } }
    }

    fun getByOrganization(organizationId: UUID?): List<TaskStage> =
            getAll().filter {
                recruitmentProcessService
                        .getProcessFromStage(it.applicationStage)
                        .offer
                        .creator
                        .organization.id == organizationId
                        && isTaskStageCurrentStage(it)
            }


    fun addResult(resultToAdd: TaskService.ResultToAdd) {
        val resultToSave = taskResultRepository.findAll().firstOrNull { it.task.id == resultToAdd.task.id && it.taskStage.id == resultToAdd.taskStage.id && it.startTime != null }.let {
            copyTaskResult(it, resultToAdd)
                ?: createNewTaskResult(resultToAdd)
        }
        val savedResult = taskResultRepository.save(resultToSave)
        val taskStage = savedResult.taskStage
        val newTaskResults = taskStage.tasksResult.filter { it.task.id != resultToSave.task.id }.plus(savedResult)
        taskStageRepository.save(taskStage.copy(tasksResult = newTaskResults))
    }
    private fun copyTaskResult(foundResult: TaskResult?, resultToAdd: TaskService.ResultToAdd) =
        foundResult?.copy(
                results = resultToAdd.results,
                code = resultToAdd.code,
                startTime = foundResult.startTime,
                endTime = resultToAdd.time,
                task = resultToAdd.task,
                taskStage = resultToAdd.taskStage
        )

    private fun createNewTaskResult(resultToAdd: TaskService.ResultToAdd): TaskResult {
        if (isFirstSolvedTask(resultToAdd))
            sendDevNotification(resultToAdd)

        return TaskResult(null, resultToAdd.results, resultToAdd.code, resultToAdd.time, null, resultToAdd.task, resultToAdd.taskStage)
    }

    private fun sendDevNotification(resultToAdd: TaskService.ResultToAdd) {
        val timeToWait = getTimeLimitsSum(resultToAdd.taskStage)
        resultToAdd.taskStage.devs.forEach {
            val offer = recruitmentProcessService.getProcessFromStage(resultToAdd.taskStage.applicationStage).offer
            mailService.sendTaskSubmittedNotification(it, resultToAdd.taskStage, timeToWait, offer)
        }
    }

    private fun isFirstSolvedTask(resultToAdd: TaskService.ResultToAdd) = resultToAdd.taskStage.tasksResult.none { it.startTime != null }
    private fun getTimeLimitsSum(taskStage: TaskStage) = taskStage.tasksResult.sumOf { it.task.timeLimit }

    fun setDevs(id: UUID, devs: MutableList<String>): TaskStage =
        getTaskStage(id).let { taskStageRepository.save(it.copy(devs = devs)) }

    fun setTasks(taskStageUuid: String, tasksIds: Set<Int>, password: String) {
        if (securityService.getTaskStageFromPassword(password)?.let {
                    it.id.toString() != taskStageUuid ||
                            !isTaskStageCurrentStage(it)
                } == true)
            throw UnauthenticatedException()
        tasksIds.mapNotNull { taskRepository.findById(it).orElse(null) }.let {
            deleteRemovedTaskResults(taskStageUuid, tasksIds)
            addMissingTaskResults(taskStageUuid, it)
        }

        getTaskStage(taskStageUuid).let {
            if (it.applicationStage.stage.type == StageType.TASK) {
                val offer = recruitmentProcessService.getProcessFromStage(it.applicationStage).offer
                val mail = it.applicationStage.application.jobSeeker.user.mail
                mailService.sendTaskAssignedNotification(mail, it, offer)
            }
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
            taskStage.applicationStage.application.getCurrentApplicationStage().id == taskStage.applicationStage.id


    private fun deleteRemovedTaskResults(taskStageUuid: String, tasksIds: Set<Int>) =
            getTaskStage(taskStageUuid).tasksResult.filterNot { tasksIds.contains(it.id) }
                    .also {
                        val taskStage = getTaskStage(taskStageUuid)
                        taskStageRepository.save(taskStage.copy(tasksResult = taskStage.tasksResult.minus(it))) }
                    .forEach{ taskResultRepository.delete(it) }


    private fun addMissingTaskResults(taskStageUuid: String, tasks: List<Task>) {
        val taskStage = getTaskStage(taskStageUuid)
        tasks.filterNot { task -> taskStage.tasksResult.map { it.task.id }.contains(task.id) }
                .map {
                    taskResultRepository.save(TaskResult(null, null, null, null, null, it, taskStage))
                }.also {
                    val stage = getTaskStage(taskStageUuid)
                    taskStageRepository.save(stage.copy(tasksResult = stage.tasksResult.plus(it)))
                }
    }
}
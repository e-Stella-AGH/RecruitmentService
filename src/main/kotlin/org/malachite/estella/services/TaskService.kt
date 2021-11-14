package org.malachite.estella.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.decodeBase64
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.commons.toBase64String
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.process.domain.*
import org.malachite.estella.queues.utils.TaskResultRabbitDTO
import org.malachite.estella.task.domain.InvalidTestFileException
import org.malachite.estella.task.domain.TaskNotFoundException
import org.malachite.estella.task.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob


@Service
class TaskService(
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val taskStageService: TaskStageService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val interviewService: InterviewService,
    @Autowired private val securityService: SecurityService
) : EStellaService<Task>() {

    override val throwable: Exception = TaskNotFoundException()

    fun checkDevPassword(organizationUuid: UUID?, password: String) =
        organizationUuid
            ?.let {
                organizationService.getOrganization(it)
            }
            ?.takeIf {
                securityService.compareOrganizationWithPassword(it, password)
            }
            ?.let { this } ?: throw UnauthenticatedException()


    fun getTasksByOrganizationUuid(organizationUuid: UUID): List<TaskDto> =
        organizationService.getOrganization(organizationUuid)
            .tasks
            .map { it.toTaskDto() }


    fun getTaskTestsWithinOrganization(organizationUuid: UUID, taskId: Int): String =
        organizationService.getOrganization(organizationUuid)
            .tasks
            .find { it.id == taskId }
            ?.tests
            ?.toBase64String() ?: throw TaskNotFoundException()

    fun getTasksByTasksStage(tasksStageId: UUID, password: String): List<TaskDto> {
        val taskStage = taskStageService.getTaskStage(tasksStageId)
        val organizationUuid = recruitmentProcessService
            .getProcessFromStage(taskStage.applicationStage)
            .offer
            .creator
            .organization
            .id
        checkDevPassword(organizationUuid, password)
        return taskStage.tasksResult.map { it.task.toTaskDto() }
    }


    fun getTaskById(taskId: Int) =
        withExceptionThrower { taskRepository.findById(taskId).get() }
            .toTaskDto()

    fun getTestsFromTask(taskId: Int): List<TaskTestCaseDto> =
        taskRepository
            .findById(taskId)
            .get()
            .tests
            .toBase64String()
            .let { Json.decodeFromString(it) }

    fun addTask(organizationUuid: UUID, taskDto: TaskDto) =
        withExceptionThrower {
            // Task saving
            checkTestsFormat(taskDto.testsBase64)
            val task = taskRepository.save(taskDto.toTask())

            // Attaching task to organization
            val organization = organizationService.getOrganization(organizationUuid)

            val updatedTasks = organization.tasks.plus(task)
            val updatedOrganization = organization.copy(tasks = updatedTasks)
            organizationRepository.save(updatedOrganization)
            task
        }

    fun updateTask(taskDto: TaskDto) = taskDto.id?.let {
        updateTask(it, taskDto.toTask())
    } ?: throw TaskNotFoundException()

    fun updateTask(id: Int, task: Task) {
        checkTestsFormat(task.toTaskDto().testsBase64)
        val currTask: Task = taskRepository.findById(id).get()
        val updated: Task = currTask.copy(
            id = task.id,
            tests = task.tests,
            description = task.description,
            timeLimit = task.timeLimit
        )
        taskRepository.save(updated)
    }

    fun deleteTask(organizationUuid: UUID, taskId: Int) {
        // Detaching task from organization
        val organization = organizationService.getOrganization(organizationUuid)
        val updatedTasks = organization.tasks.filter {
            it.id != taskId
        }.toSet()
        val updatedOrganization = organization.copy(tasks = updatedTasks)
        organizationRepository.save(updatedOrganization)

        // Removing task from db
        taskRepository.deleteById(taskId)
    }


    fun setTests(taskId: Int, testsBase64: String) = taskRepository.findById(taskId)
        .get()
        .let {
            checkTestsFormat(testsBase64)
            updateTask(taskId, it.copy(tests = testsBase64.decodeBase64().toByteArray().toTypedArray()))
        }

    fun setTests(taskId: Int, tests: List<TaskTestCaseDto>) =
        taskRepository.findById(taskId)
            .get()
            .let {
                updateTask(
                    taskId,
                    it.copy(tests = tests.encodeToJson().toByteArray().toTypedArray())
                )
            }

    fun addResult(result: TaskResultRabbitDTO) {
        val taskStage = taskStageService.getTaskStage(result.solverId)
        val task = getTaskById(result.taskId).toTask()
        val startTime = result.startTime
            ?.takeIf { it != "null" }
            ?.let { Timestamp.valueOf(it) }
        val endTime = result.endTime
            ?.takeIf { it != "null" }
            ?.let { Timestamp.valueOf(it) }

        taskStageService.addResult(TaskResult(null,
            SerialBlob(Base64.getEncoder().encode(result.results.toByteArray())),
            SerialClob(result.code.toCharArray()),
            startTime,
            endTime,
            task,
            taskStage
        ))
    }


    private fun checkTestsFormat(tests: String) = try {
        val decoded = String(Base64.getDecoder().decode(tests))
        TaskTestCaseDto.decodeFromJson(decoded)
    } catch (e: SerializationException) {
        e.printStackTrace()
        throw InvalidTestFileException()
    }

    fun checkOrganizationRights(organizationUuid: UUID, taskId: Int): TaskService =
        try {
            organizationService
                .getOrganization(organizationUuid)
                .tasks
                .find { it.id == taskId } ?: throw TaskNotFoundException()
            this
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnauthenticatedException()
        }

}
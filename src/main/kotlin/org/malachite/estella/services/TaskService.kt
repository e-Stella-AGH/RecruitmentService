package org.malachite.estella.services

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.process.domain.*
import org.malachite.estella.task.domain.TaskNotFoundException
import org.malachite.estella.task.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.sql.Blob
import java.util.*
import javax.sql.rowset.serial.SerialBlob


@Service
class TaskService(
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService
) : EStellaService<Task>() {

    override val throwable: Exception = TaskNotFoundException()

    fun checkDevPassword(organizationUuid: String, password: String) =
        organizationService.getOrganization(organizationUuid).let {
            if (!securityService.compareOrganizationWithPassword(it, password))
                throw UnauthenticatedException()
            this
        }


    fun getTasksByOrganizationUuid(organizationUuid: String) =
        organizationService.getOrganization(organizationUuid)
            .tasks
            .map { it.toTaskDto() }

    fun getTaskById(taskId: Int) =
        withExceptionThrower { taskRepository.findById(taskId).get() }
            .toTaskDto()

    fun getTestsFromTask(taskId: Int): List<TaskTestCaseDto> =
        taskRepository
            .findById(taskId)
            .get()
            .tests.binaryStream
            .readAllBytes()
            .contentToString()
            .let { Json.decodeFromString<List<TaskTestCaseDto>>(it) }

    fun addTask(task: TaskDto) =
        withExceptionThrower {
            if (!checkTestsFormat(task.testsBase64)) throw IllegalArgumentException("Tests in wrong format")
            taskRepository.save(task.toTask())
        }.toTaskDto()

    fun updateTask(id: Int, task: Task) {
        if (!checkTestsFormat(task.toTaskDto().testsBase64)) throw IllegalArgumentException()
        val currTask: Task = taskRepository.findById(id).get()
        val updated: Task = currTask.copy(
            id = task.id,
            tests = task.tests,
            description = task.description,
            timeLimit = task.timeLimit
        )
        taskRepository.save(updated)
    }

    fun setTests(taskId: Int, testsBase64: String) = taskRepository.findById(taskId)
        .get()
        .let {
            if (!checkTestsFormat(testsBase64)) throw IllegalArgumentException()
            updateTask(taskId, it.copy(tests = SerialBlob(testsBase64.toByteArray())))
        }

    fun setTests(taskId: Int, tests: List<TaskTestCaseDto>) =
        taskRepository.findById(taskId)
            .get()
            .let {
                updateTask(
                    taskId,
                    it.copy(tests = SerialBlob(Base64.getEncoder().encode(tests.toString().encodeToByteArray())))
                )
            }


    private fun checkTestsFormat(tests: String) = try {
        val decoded = String(Base64.getDecoder().decode(tests))
        Json.decodeFromString<List<TaskTestCaseDto>>(decoded)
        true
    } catch (e: SerializationException) {
        e.printStackTrace()
        false
    }

}
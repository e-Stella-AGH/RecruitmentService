package org.malachite.estella.services

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskTest
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestDto
import org.malachite.estella.process.domain.toTaskDto
import org.malachite.estella.task.domain.TaskNotFoundException
import org.malachite.estella.task.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Blob
import java.util.*
import javax.sql.rowset.serial.SerialBlob



@Service
class TaskService(
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val applicationService: ApplicationService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService
) : EStellaService<Task>() {

    override val throwable: Exception = TaskNotFoundException()

    fun getTasksByRecruitmentProcess(processId: Int) =
        recruitmentProcessService.getProcess(processId).tasks
            .map { it.toTaskDto() }

    fun getTaskById(taskId: Int) =
        withExceptionThrower { taskRepository.findById(taskId).get() }
            .toTaskDto()

    fun getTasksTests(taskId: Int) =
         Json
             .decodeFromString<List<TaskTestDto>>(taskRepository
                 .findById(taskId)
                 .get()
                 .tests.binaryStream
                 .readAllBytes()
                 .contentToString()) // try catch this mf


    fun addTask(task: TaskDto) =
        withExceptionThrower {
            checkTestsFormat(task.tests)
            .let { taskRepository.save(TaskDto.toTask(task)) }
        }
            .toTaskDto()

    fun updateTask(id: Int,  task: Task) {
        val currTask: Task = taskRepository.findById(id).get()
        val updated: Task = currTask.copy(
            id = task.id,
            tests = task.tests,
            description = task.description,
            timeLimit = task.timeLimit,
            deadline = task.deadline
        )
        taskRepository.save(updated)
    }

    fun setTests(taskId: Int, tests: Blob) = taskRepository.findById(taskId).get().let {
        updateTask(taskId, it.copy(tests = tests))
    }

    fun setTests(taskId: Int, tests: List<TaskTestDto>) =
        setTests(taskId, SerialBlob(Base64.getEncoder().encode(tests.toString().encodeToByteArray())))





    private fun checkTestsFormat(tests: String) = true //TODO

}
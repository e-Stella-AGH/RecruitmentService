package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestDto
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.Blob

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    @Autowired private val taskService: TaskService,
    @Autowired private val processService: RecruitmentProcessService
) {

    @CrossOrigin
    @GetMapping("/{processId}")
    fun getTaskByRecruitmentProcess(@PathVariable("processId") processId: Int): ResponseEntity<List<TaskDto>> =
        taskService
            .getTasksByRecruitmentProcess(processId)
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/{taskId}")
    fun getTaskById(@PathVariable taskId: Int): ResponseEntity<TaskDto> = taskService.getTaskById(taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}/tests")
    fun getTaskTests(@PathVariable taskId: Int): ResponseEntity<List<TaskTestDto>> = taskService.getTasksTests(taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @PostMapping("/{processId}/add-task")
    fun addTask(
        @PathVariable processId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody task: TaskDto
    ) = taskService.addTask(task).let{ t ->
        processService.getProcess(processId)
            .let {
                processService.updateProcess(processId, it.copy(tasks = it.tasks.plus(t) as Set<Task>))
            }
    }

    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/file")
    fun setTestsWithFile(@PathVariable taskId: Int,
    @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
    @RequestBody tests: Blob
    ) = taskService.setTests(taskId, tests)

    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/object")
    fun setTestsWithObject(@PathVariable taskId: Int,
                            @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
                            @RequestBody tests: List<TaskTestDto>
    ) = taskService.setTests(taskId, tests)



}
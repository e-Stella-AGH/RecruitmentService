package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    @Autowired private val taskService: TaskService,
    @Autowired private val processService: RecruitmentProcessService
) {

    @CrossOrigin
    @Transactional
    @GetMapping
    fun getTaskByRecruitmentProcess(@RequestParam(name = "process") processId: Int): ResponseEntity<List<TaskDto>> =
        taskService
            .getTasksByRecruitmentProcess(processId)
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}")
    fun getTaskById(@PathVariable taskId: Int): ResponseEntity<TaskDto> = taskService.getTaskById(taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}/tests")
    fun getTaskTests(@PathVariable taskId: Int): ResponseEntity<List<TaskTestCaseDto>> = taskService.getTasksTests(taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @PostMapping("/{processId}")
    fun addTask(
        @PathVariable processId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody task: TaskDto
    ) = taskService.addTask(task).let { t ->
        processService.getProcess(processId)
            .let {
                processService.updateTasks(processId, setOf(t.toTask()))
            }
    }
        .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/file")
    fun setTestsWithFile(
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody testsBase64: String
    ) = taskService.setTests(taskId, testsBase64)

    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/object")
    fun setTestsWithObject(
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody tests: List<TaskTestCaseDto>
    ) = taskService.setTests(taskId, tests)

}
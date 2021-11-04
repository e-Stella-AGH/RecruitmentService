package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.services.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Transactional
@RequestMapping("/api/tasks")
class TaskController(
    @Autowired private val taskService: TaskService,
) {

    private val bothUUIDErrorMessage = Message("Exactly one of parameters: organizationUuid and taskStageUuid is required")

    @Deprecated("Not tested yet - draft implementation, should be tested as part of ES-162")
    @CrossOrigin
    @GetMapping
    fun getTasks(
        @RequestParam("owner", required = false) organizationUuid: UUID?,
        @RequestParam("taskStage", required = false) taskStageUuid: UUID?,
        @RequestHeader(EStellaHeaders.devPassword) password: String
    ): ResponseEntity<Any> {
        if (listOfNotNull(organizationUuid, taskStageUuid).size != 1)
            return ResponseEntity.badRequest().body(bothUUIDErrorMessage)
        val tasks: List<TaskDto> = organizationUuid
            ?.let {
                taskService.checkDevPassword(it, password)
                    .getTasksByOrganizationUuid(it)
            }
            ?: taskStageUuid?.let { taskService.getTasksByTasksStage(it, password) }!!
       return ResponseEntity.ok(tasks)
    }

    @CrossOrigin
    @GetMapping("/{taskId}")
    fun getTaskById(
        @RequestParam("owner") organizationUuid: UUID,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.devPassword) password: String
    ) = taskService
        .checkDevPassword(organizationUuid, password)
        .checkOrganizationRights(organizationUuid, taskId)
        .getTaskById(taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/{taskId}/tests")
    fun getTaskTests(
        @RequestParam("owner") organizationUuid: UUID,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.devPassword) password: String
    ) = taskService
        .checkDevPassword(organizationUuid, password)
        .getTaskTestsWithinOrganization(organizationUuid, taskId)
        .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @PostMapping
    fun addTask(
        @RequestParam("owner") organizationUuid: UUID,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody task: TaskDto
    ) = taskService.checkDevPassword(organizationUuid, password)
        .addTask(organizationUuid, task)
        .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @RequestParam("owner") organizationUuid: UUID,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @PathVariable taskId: Int
    ) = taskService
        .checkDevPassword(organizationUuid, password)
        .checkOrganizationRights(organizationUuid, taskId)
        .deleteTask(organizationUuid, taskId)
        .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping
    fun updateTask(
        @RequestParam("owner") organizationUuid: UUID,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody taskDto: TaskDto
    ) = taskDto.id?.let { taskId ->
        taskService
            .checkDevPassword(organizationUuid, password)
            .checkOrganizationRights(organizationUuid, taskId)
            .updateTask(taskDto)
            .let { OwnResponses.SUCCESS }
    }

    @CrossOrigin
    @PutMapping("/{taskId}/tests/file")
    fun setTestsWithFile(
        @RequestParam("owner") organizationUuid: UUID,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody testFile: TestFilePayload
    ) = taskService
        .checkDevPassword(organizationUuid, password)
        .checkOrganizationRights(organizationUuid, taskId)
        .setTests(taskId, testFile.testsBase64)
        .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{taskId}/tests/object")
    fun setTestsWithObject(
        @RequestParam("owner") organizationUuid: UUID,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody tests: TaskTestObjectPayload
    ) = taskService
        .checkDevPassword(organizationUuid, password)
        .checkOrganizationRights(organizationUuid, taskId)
        .setTests(taskId, tests.tests)
        .let { OwnResponses.SUCCESS }

    data class TaskTestObjectPayload(val tests: List<TaskTestCaseDto>)

    data class TestFilePayload(val testsBase64: String)
}
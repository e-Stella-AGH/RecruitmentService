package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.services.TaskService
import org.malachite.estella.services.TaskStageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/tasks")
class TaskController(
        @Autowired private val taskService: TaskService,
        @Autowired private val taskStageService: TaskStageService,
) {

    @CrossOrigin
    @GetMapping
    fun getTasks(
            @RequestParam("owner") organizationUuid: String?,
            @RequestParam("taskStage") taskStageUuid: String?,
            @RequestParam("devMail") devMail: String?,
            @RequestParam("interview") interviewUuid: String?,
            @RequestHeader(EStellaHeaders.devPassword) password: String?
    ): ResponseEntity<Any> {
        if (!areParamsValid(organizationUuid, taskStageUuid, devMail, interviewUuid))
            return ResponseEntity.badRequest().body(Message("Exactly one of parameters: organizationUuid, taskStageUuid, devMail is required"))
        if (!isPasswordProvided(organizationUuid, taskStageUuid, devMail, password) && interviewUuid == null)
            return OwnResponses.UNAUTH
        val tasks: List<TaskDto> =
                when {
                    organizationUuid != null -> devMail?.let { taskStageService.getTasksByDev(it, password!!) }
                                        ?: taskStageService.assertDevPasswordCorrect(organizationUuid, password!!)
                                                .getTasksByOrganizationUuid(UUID.fromString(organizationUuid))

                    taskStageUuid != null -> taskStageService.getTasksByTasksStage(taskStageUuid, password!!)
                    interviewUuid != null -> taskStageService.getTasksByInterview(interviewUuid)
                    else -> throw IllegalStateException() // Should never happen - protected by areParamsValid()
                }
        return ResponseEntity.ok(tasks)
    }

    private fun areParamsValid(organizationId: String?, taskStageUuid: String?, devMail: String?, interviewUuid: String?): Boolean =
            listOf(
                    (listOfNotNull(organizationId, taskStageUuid, devMail, interviewUuid).isEmpty()),
                    (listOfNotNull(organizationId, taskStageUuid, interviewUuid).size > 1),
                    (devMail != null && organizationId == null)
            ).none{ it }

    private fun isPasswordProvided(organizationId: String?, taskStageUuid: String?, devMail: String?, password: String?): Boolean =
            listOfNotNull(organizationId, taskStageUuid, devMail).isNotEmpty() && password != null

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

data class Tasks(val tasks: Set<Int>)
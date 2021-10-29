package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.interview.api.MeetingNotes
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/tasks")
class TaskController(
        @Autowired private val taskService: TaskService,
        @Autowired private val organizationService: OrganizationService,
) {

    @Deprecated("Not tested yet - draft implementation, should be tested as part of ES-162")
    @CrossOrigin
    @Transactional
    @GetMapping
    fun getTasks(
            @RequestParam("owner", required = false) organizationUuid: String?,
            @RequestParam("taskStage", required = false) taskStageUuid: String?,
            @RequestHeader(EStellaHeaders.devPassword) password: String
    ): ResponseEntity<Any> {
        if ((organizationUuid == null && taskStageUuid == null) || (organizationUuid != null && taskStageUuid != null))
            return ResponseEntity.badRequest().body(Message("Exactly one of parameters: organizationUuid and taskStageUuid is required"))
        val tasks: List<TaskDto> = organizationUuid
                ?.let {
                    taskService.checkDevPassword(it, password)
                            .getTasksByOrganizationUuid(it)
                }
                ?: taskStageUuid
                        ?.let { taskService.getTasksByTasksStage(it, password) }!!
       return ResponseEntity.ok(tasks)
    }


    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}")
    fun getTaskById(
            @RequestParam("owner") organizationUuid: String,
            @PathVariable taskId: Int,
            @RequestHeader(EStellaHeaders.devPassword) password: String
    ) =
            ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED) /*taskService.getTaskById(taskId)
        .let { ResponseEntity.ok(it) }*/

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}/tests")
    fun getTaskTests(
            @RequestParam("owner") organizationUuid: String,
            @PathVariable taskId: Int,
            @RequestHeader(EStellaHeaders.devPassword) password: String
    ) =
            ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)
    /*taskService.getTestsFromTask(taskId)
        .let { ResponseEntity.ok(it) } */

    @CrossOrigin
    @Transactional
    @PostMapping
    fun addTask(
            @RequestParam("owner") organizationUuid: String,
            @RequestHeader(EStellaHeaders.devPassword) password: String,
            @RequestBody task: TaskDto
    ) = taskService.checkDevPassword(organizationUuid, password).addTask(task)
            .let { organizationService.updateTasks(organizationUuid, setOf(it)) }
            .let { OwnResponses.SUCCESS }

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/file")
    fun setTestsWithFile(
            @RequestParam("owner") organizationUuid: String,
            @PathVariable taskId: Int,
            @RequestHeader(EStellaHeaders.devPassword) password: String,
            @RequestBody testsBase64: String
    ) = ResponseEntity(
            Message("Not Implemented"),
            HttpStatus.NOT_IMPLEMENTED
    ) //taskService.setTests(taskId, testsBase64)

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/object")
    fun setTestsWithObject(
            @RequestParam("owner") organizationUuid: String,
            @PathVariable taskId: Int,
            @RequestHeader(EStellaHeaders.devPassword) password: String,
            @RequestBody tests: List<TaskTestCaseDto>
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED) //taskService.setTests(taskId, tests)


}
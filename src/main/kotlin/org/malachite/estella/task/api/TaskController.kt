package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.websocket.server.PathParam


@RestController
@RequestMapping("/api/tasks")
class TaskController(
    @Autowired private val taskService: TaskService,
    @Autowired private val organizationService: OrganizationService,
) {

    @CrossOrigin
    @Transactional
    @GetMapping
    fun getTaskByRecruitmentProcess(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.passwordHeader) password: String
    ): ResponseEntity<List<TaskDto>> =
        taskService.checkDevPassword(organizationUuid, password)
            .also { println("Tutaj jebło") }
            .getTasksByOrganizationUuid(organizationUuid)
            .also { println("Tutaj jebło x2") }
            .let { ResponseEntity.ok(it) }

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @GetMapping("/{taskId}")
    fun getTaskById(
        @RequestParam("owner") organizationUuid: String,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.passwordHeader) password: String
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
        @RequestHeader(EStellaHeaders.passwordHeader) password: String
    ) =
        ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)
    /*taskService.getTestsFromTask(taskId)
        .let { ResponseEntity.ok(it) } */

    @CrossOrigin
    @Transactional
    @PostMapping
    fun addTask(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.passwordHeader) password: String,
        @RequestBody task: TaskDto
    ) = taskService.checkDevPassword(organizationUuid, password).addTask(task)
        .let { t -> organizationService.updateTasks(organizationUuid, setOf(t.toTask())) }
        .let { OwnResponses.SUCCESS }

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-17 epic")
    @CrossOrigin
    @Transactional
    @PutMapping("/{taskId}/tests/file")
    fun setTestsWithFile(
        @RequestParam("owner") organizationUuid: String,
        @PathVariable taskId: Int,
        @RequestHeader(EStellaHeaders.passwordHeader) password: String,
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
        @RequestHeader(EStellaHeaders.passwordHeader) password: String,
        @RequestBody tests: List<TaskTestCaseDto>
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED) //taskService.setTests(taskId, tests)

}
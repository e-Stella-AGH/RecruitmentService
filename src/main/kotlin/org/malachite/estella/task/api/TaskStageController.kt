package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.TaskStageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/taskStages")
class TaskStageController(
    @Autowired private val taskStageService: TaskStageService,
    @Autowired private val organizationService: OrganizationService,
) {

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-162 epic")
    @CrossOrigin
    @Transactional
    @GetMapping("")
    fun getAllTaskStages(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.devPassword) password: String
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)
//
    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-162 epic")
    @CrossOrigin
    @Transactional
    @GetMapping("/dev")
    fun getAllTaskStagesByDev(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.devPassword) password: String
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)


    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-162 epic")
    @CrossOrigin
    @Transactional
    @PostMapping("/taskStage")
    fun setTasks(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody task: TaskDto
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)

    @Deprecated(message = "Wasn't tested yet - unnecessary now - to be implemented and tested in ES-162 epic")
    @CrossOrigin
    @Transactional
    @PostMapping("/interview")
    fun setTasksByInterview(
        @RequestParam("owner") organizationUuid: String,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody task: TaskDto
    ) = ResponseEntity(Message("Not Implemented"), HttpStatus.NOT_IMPLEMENTED)
}
package org.malachite.estella.task.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.TaskService
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
    @Autowired private val taskService: TaskService,
    @Autowired private val organizationService: OrganizationService,
) {

    @CrossOrigin
    @Transactional
    @PutMapping
    fun setTasks(
            @RequestParam("taskStage", required = false) taskStageUuid: String?,
            @RequestParam("interview", required = false) interviewUuid: String?,
            @RequestHeader(EStellaHeaders.devPassword) password: String,
            @RequestBody tasks: Tasks
    ): ResponseEntity<Any> {
        if (listOfNotNull(taskStageUuid, interviewUuid).size != 1)
            return ResponseEntity.badRequest().body(Message("Exactly one of parameters: organizationUuid and taskStageUuid is required"))
        taskStageUuid?.let {
            taskStageService.checkDevPasswordFromTaskStage(it, password)
                    .setTasks(it, tasks.tasks, password)
                    .let { return OwnResponses.SUCCESS }
        } ?: interviewUuid!!.let {
            taskStageService.checkDevPasswordFromInterviewUuid(it, password)
                    .setTasksByInterviewUuid(it, tasks.tasks, password)
                    .let { return OwnResponses.SUCCESS }
        }
    }

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
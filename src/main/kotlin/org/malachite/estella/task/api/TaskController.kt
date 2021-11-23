package org.malachite.estella.task.api

import org.malachite.estella.commons.*
import org.malachite.estella.process.domain.*
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.TaskService
import org.malachite.estella.services.TaskStageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/tasks")
class TaskController(
        @Autowired private val taskService: TaskService,
        @Autowired private val taskStageService: TaskStageService,
        @Autowired private val organizationService: OrganizationService,
) {

    @CrossOrigin
    @Transactional
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
                    interviewUuid != null -> taskStageService.getTasksByInterview(interviewUuid).map { it.task.toTaskDto() }
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
    @Transactional
    @GetMapping("/inProgress")
    fun getTasksForJobSeeker(
            @RequestParam("taskStage") taskStageUuid: String?,
            @RequestParam("interview") interviewUuid: String?
    ): ResponseEntity<List<TaskInProgressDto>> =
        if (listOfNotNull(taskStageUuid, interviewUuid).size != 1)
            throw BadParamsException("Exactly one of parameters: taskStage, interview is required")
        else
            when {
                taskStageUuid != null -> taskStageService.getTaskStage(taskStageUuid).tasksResult.map { it.toTaskInProgressDto() }
                interviewUuid != null -> taskStageService.getTasksByInterview(interviewUuid).map { it.toTaskInProgressDto() }
                else -> throw IllegalStateException() // Should never happen - protected by areParamsValid()
            }.let { ResponseEntity.ok(it) }


    @CrossOrigin
    @Transactional
    @PutMapping("/taskStarted")
    fun startTask(
            @RequestParam("taskStage") taskStageUuid: String,
            @RequestParam("task") taskId: Int
    ) = taskStageService.startTask(taskStageUuid, taskId)
                .let { OwnResponses.SUCCESS }


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
            @RequestParam("owner") organizationUuid: PayloadUUID,
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

data class Tasks(val tasks: Set<Int>)
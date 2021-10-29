package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.*
import org.malachite.estella.commons.OwnResponses.CREATED
import org.malachite.estella.commons.OwnResponses.SUCCESS
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.interview.api.MeetingNotes
import org.malachite.estella.services.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Transactional
@RequestMapping("/api/applications")
class ApplicationController(
    @Autowired private val applicationService: ApplicationService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val applicationStageDataService: ApplicationStageDataService,
    @Autowired private val securityService: SecurityService
) {

    @CrossOrigin
    @PostMapping("/apply/{offerId}/user")
    fun applyForAnOffer(
        @PathVariable offerId: Int,
        @RequestBody applicationPayload: ApplicationLoggedInPayload
    ) =
        securityService.getJobSeekerFromContext()
            ?.let { applicationService.insertApplicationLoggedInUser(offerId, it, applicationPayload) }
            ?.let { CREATED(it.toApplicationDTO()) }
            ?: UNAUTH

    @CrossOrigin
    @PostMapping("/apply/{offerId}/no-user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload)
            : ResponseEntity<ApplicationDTO> =
        applicationService
            .insertApplicationWithoutUser(offerId, applicationPayload)
            .let { CREATED(it.toApplicationDTO()) }


    @CrossOrigin
    @GetMapping("/{applicationId}")
    fun getApplicationById(@PathVariable applicationId: Int): ResponseEntity<ApplicationDTO> =
        applicationService
            .getApplicationById(applicationId)
            .let { ResponseEntity.ok(it.toApplicationDTO()) }

    @CrossOrigin
    @GetMapping("/")
    fun getAllApplications(): ResponseEntity<List<ApplicationDTO>> =
        applicationService
            .getAllApplications()
            .map { it.toApplicationDTO() }
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/offer/{offerId}")
    fun getAllApplicationsByOffer(@PathVariable offerId: Int): ResponseEntity<List<ApplicationDTOWithStagesListAndOfferName>> =
        applicationService
            .getApplicationsWithStagesAndOfferName(offerId)
            .map { it.first.toApplicationDTOWithStagesListAndOfferName(it.second, it.third) }
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/job-seeker")
    fun getAllApplicationsByJobSeeker(): ResponseEntity<Any> =
        securityService.getJobSeekerFromContext()
            ?.id
            ?.let { applicationService.getApplicationsByJobSeeker(it) }
            ?.map { it.toApplicationDTO() }
            ?.let { ResponseEntity.ok(it) }
            ?: UNAUTH


    @CrossOrigin
    @PutMapping("/{applicationId}/next")
    fun updateApplicationStage(
        @PathVariable applicationId: Int
    ): ResponseEntity<Any> =
        applicationService.getApplicationById(applicationId).let {
            recruitmentProcessService.getProcessFromStage(it.applicationStages.last())
        }.let {
            if (!securityService.checkOfferRights(it.offer)) return UNAUTH
            applicationService.setNextStageOfApplication(applicationId, it).let { SUCCESS }
        }


    @CrossOrigin
    @PutMapping("/{applicationId}/reject")
    fun rejectApplication(
        @PathVariable applicationId: Int
    ): ResponseEntity<Any> =
        applicationService.getApplicationById(applicationId)
            .let {
                recruitmentProcessService.getProcessFromStage(it.applicationStages.first())
            }
            .let {
                if (!securityService.checkOfferRights(it.offer)) return UNAUTH
                applicationService.rejectApplication(applicationId).let { SUCCESS }
            }

    @CrossOrigin
    @Transactional
    @PutMapping("/add_notes")
    fun addNotes(
        @RequestParam("cv_note") applicationId: Int?,
        @RequestParam("task_note") taskStageUUID: PayloadUUID?,
        @RequestParam("interview_note") interviewUUID: PayloadUUID?,
        @RequestHeader(EStellaHeaders.devPassword) password: String?,
        @RequestBody notes: MeetingNotes
    ): ResponseEntity<Any> =
        when {
            applicationId != null -> applicationService.getApplicationById(applicationId)
                .let { applicationStageDataService.setNotesToApplied(it, password, notes.notes) }
            taskStageUUID != null -> applicationStageDataService.setNotesToTaskStage(
                taskStageUUID.toUUID(),
                password,
                notes.notes
            )
            interviewUUID != null -> applicationStageDataService.setNotesToInterview(
                interviewUUID.toUUID(),
                password,
                notes.notes
            )
            else -> throw NoteNotAttachedException()
        }.let {
            SUCCESS
        }

    @CrossOrigin
    @Transactional
    @GetMapping("/get_notes")
    fun getNotesFromStage(
        @RequestParam("cv_note") applicationId: Int?,
        @RequestParam("task_note") taskStageUUID: PayloadUUID?,
        @RequestParam("interview_note") interviewUUID: PayloadUUID?,
        @RequestParam("with_tasks") includeTasks: Boolean = false,
        @RequestHeader(EStellaHeaders.devPassword) password: String?
    ): ResponseEntity<Any> =
        when {
            applicationId != null -> applicationService.getApplicationById(applicationId)
                .let { applicationStageDataService.getNotesByApplication(it, password) }
                .toApplicationNotesDTO()
            taskStageUUID != null && includeTasks -> applicationStageDataService.getNotesByTaskIdWithTask(
                taskStageUUID.toUUID(),
                password
            ).toTasksNotesDTO()
            taskStageUUID != null && !includeTasks -> applicationStageDataService.getNotesByTaskId(
                taskStageUUID.toUUID(),
                password
            ).toApplicationNotesDTO()
            interviewUUID != null -> applicationStageDataService.getNotesByInterviewId(interviewUUID.toUUID(), password)
                .toApplicationNotesDTO()
            else -> throw NotSpecifiedWhichNoteGet()
        }.let { response: Any ->
            ResponseEntity.ok(response)
        }

}
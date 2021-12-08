package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses.CREATED
import org.malachite.estella.commons.OwnResponses.SUCCESS
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.commons.PayloadUUID
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.interview.api.MeetingNotes
import org.malachite.estella.people.domain.toJobSeekerDTO
import org.malachite.estella.people.domain.toJobSeekerFileDTO
import org.malachite.estella.services.ApplicationService
import org.malachite.estella.services.ApplicationStageDataService
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

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
    fun getAllApplicationsByOffer(@PathVariable offerId: Int): ResponseEntity<List<ApplicationInfoDTO>> =
        applicationService
            .getApplicationsWithStagesAndOfferName(offerId)
            .map { it.toApplicationInfoDTO() }
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/job-seeker")
    fun getAllApplicationsByJobSeeker(): ResponseEntity<List<ApplicationDTOWithStagesListAndOfferName>> =
        securityService.getJobSeekerFromContext()
            ?.id
            ?.let { applicationService.getApplicationsByJobSeeker(it) }
            ?.map { it.toApplicationDtoWithStagesListAndOfferName() }
            ?.let { ResponseEntity.ok(it) }
            ?: throw UnauthenticatedException()

    private fun ApplicationService.ApplicationWithStagesAndOfferName.toApplicationDtoWithStagesListAndOfferName() = ApplicationDTOWithStagesListAndOfferName(
        this.application.id,
        this.application.applicationDate,
        this.application.status,
        this.application.applicationStages.maxByOrNull { it.id!! }!!.stage,
        this.application.jobSeeker.toJobSeekerDTO(),
        this.application.seekerFiles.map { it.toJobSeekerFileDTO() }.toSet(),
        this.stages,
        this.offerName
    )

    private fun ApplicationService.ApplicationInfo.toApplicationInfoDTO() = ApplicationInfoDTO(
        id,
        applicationDate,
        status,
        stage,
        jobSeeker.toJobSeekerDTO(),
        seekerFiles.map { it.toJobSeekerFileDTO() }.toSet(),
        stages,
        offerName,
        tags
    )


    @CrossOrigin
    @PutMapping("/{applicationId}/next")
    fun updateApplicationStage(
        @PathVariable applicationId: Int,
        @RequestBody(required = false) devs: ApplicationStageDevs?
    ): ResponseEntity<Message> =
        applicationService.getApplicationById(applicationId).let {
            recruitmentProcessService.getProcessFromStage(it.applicationStages.last())
        }.let {
            if (!securityService.checkOfferRights(it.offer)) throw UnauthenticatedException()
            applicationService.setNextStageOfApplication(applicationId, it, devs?.devs?: mutableListOf()).let { SUCCESS }
        }


    @CrossOrigin
    @PutMapping("/{applicationId}/reject")
    fun rejectApplication(
        @PathVariable applicationId: Int
    ): ResponseEntity<Message> =
        applicationService.getApplicationById(applicationId)
            .let {
                recruitmentProcessService.getProcessFromStage(it.applicationStages.first())
            }
            .let {
                if (!securityService.checkOfferRights(it.offer)) throw UnauthenticatedException()
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
    ): ResponseEntity<Message> =
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
            else -> throw NotSpecifiedWhichNoteToGet()
        }.let { response: Any ->
            ResponseEntity.ok(response)
        }


    @CrossOrigin
    @Transactional
    @GetMapping("/forDev/{devMail}")
    fun getApplicationsForDev(
            @PathVariable("devMail") devMail: String,
            @RequestHeader(EStellaHeaders.devPassword) password: String
    ): ResponseEntity<List<ApplicationForDevDTO>> =
            applicationService.getApplicationsForDev(
                    String(Base64.getDecoder().decode(devMail.toByteArray())),
                    password
            ).let { ResponseEntity.ok(it) }

}

data class ApplicationStageDevs(val devs: MutableList<String>?)
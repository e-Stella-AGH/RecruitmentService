package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.OwnResponses.CREATED
import org.malachite.estella.commons.OwnResponses.SUCCESS
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.services.ApplicationService
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
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
            ?.let { CREATED(it) }
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
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/")
    fun getAllApplications(): ResponseEntity<List<ApplicationDTO>> =
        applicationService
            .getAllApplications()
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/offer/{offerId}")
    fun getAllApplicationsByOffer(@PathVariable offerId: Int): ResponseEntity<List<ApplicationDTOWithStagesListAndOfferName>> =
        applicationService
            .getApplicationsByOffer(offerId)
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/job-seeker")
    fun getAllApplicationsByJobSeeker(): ResponseEntity<Any> =
        securityService.getJobSeekerFromContext()
            ?.id
            ?.let { applicationService.getApplicationsByJobSeeker(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: UNAUTH


    @CrossOrigin
    @PutMapping("/{applicationId}/next")
    fun updateApplicationStage(
        @PathVariable applicationId: Int
    ): ResponseEntity<Any> =
        applicationService.getApplicationById(applicationId).let {
            recruitmentProcessService.getProcessFromStage(it.stage)
        }.let {
            if (!securityService.checkOfferRights(it.offer)) return UNAUTH
            applicationService.setNextStageOfApplication(applicationId).let { SUCCESS }
        }


    @CrossOrigin
    @PutMapping("/{applicationId}/reject")
    fun rejectApplication(
        @PathVariable applicationId: Int
    ): ResponseEntity<Any> =
        applicationService.getApplicationById(applicationId)
            .let {
                recruitmentProcessService.getProcessFromStage(it.stage) }
            .let {
                if (!securityService.checkOfferRights(it.offer)) return UNAUTH
                applicationService.rejectApplication(applicationId).let { SUCCESS }
        }
}
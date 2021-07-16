package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.ApplicationLoggedInPayload
import org.malachite.estella.aplication.domain.ApplicationNoUserPayload
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.services.ApplicationService
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@Transactional
@RequestMapping("/api/applications")
class ApplicationController(
    @Autowired private val applicationService: ApplicationService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService
) {

    @CrossOrigin
    @PostMapping("/apply/{offerId}/user")
    fun applyForAnOffer(@PathVariable offerId: Int, @CookieValue("jwt") jwt: String?, @RequestBody applicationPayload: ApplicationLoggedInPayload): ResponseEntity<Any> {
        val jobSeeker = securityService.getJobSeekerFromJWT(jwt) ?: return ResponseEntity.status(404).body("Unauthenticated")
        val saved = applicationService.insertApplicationLoggedInUser(offerId,jobSeeker, applicationPayload)
        return ResponseEntity.created(URI("/api/applications/" + saved.id)).build()
    }

    @CrossOrigin
    @PostMapping("/apply/{offerId}/no-user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload): SuccessMessage =
        applicationService.insertApplicationWithoutUser(offerId, applicationPayload).let { SuccessMessage }


    @CrossOrigin
    @GetMapping("/{applicationId}")
    fun getApplicationById(@PathVariable applicationId: Int) =
        applicationService.getApplicationById(applicationId)

    @CrossOrigin
    @GetMapping("/")
    fun getAllApplications() =
        applicationService.getAllApplications()

    @CrossOrigin
    @GetMapping("/offer/{offerId}")
    fun getAllApplicationsByOffer(@PathVariable offerId: Int) =
        applicationService.getApplicationsByOffer(offerId)

    @CrossOrigin
    @GetMapping("/job-seeker/{jobSeekerId}")
    fun getAllApplicationsByJobSeeker(@PathVariable jobSeekerId: Int) =
            applicationService.getApplicationsByJobSeeker(jobSeekerId)

    @CrossOrigin
    @DeleteMapping("/delete/{applicationId}")
    fun deleteApplication(@PathVariable applicationId: Int) =
        applicationService.deleteApplication(applicationId)

    @CrossOrigin
    @PutMapping("/{applicationId}/next")
    fun updateApplicationStage(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?, @PathVariable applicationId: Int): ResponseEntity<Message> {
        applicationService.getApplicationById(applicationId).let {
            recruitmentProcessService.getProcessFromStage(it.stage).let {
                if (!checkUserRights(it.offer, jwt)) return OwnResponses.UNAUTH
                return applicationService.setNextStageOfApplication(applicationId).let { OwnResponses.SUCCESS }
            }
        }
    }

    @CrossOrigin
    @PutMapping("/{applicationId}/reject")
    fun rejectApplication(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?, @PathVariable applicationId: Int): ResponseEntity<Message> {
        applicationService.getApplicationById(applicationId).let {
            recruitmentProcessService.getProcessFromStage(it.stage).let {
                if (!checkUserRights(it.offer, jwt)) return OwnResponses.UNAUTH
                return applicationService.rejectApplication(applicationId).let { OwnResponses.SUCCESS }

            }
        }
    }

    private fun checkUserRights(offer: Offer, jwt: String?) =
        securityService.checkOfferRights(offer, jwt)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Any> {
        ex.printStackTrace()
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }


}
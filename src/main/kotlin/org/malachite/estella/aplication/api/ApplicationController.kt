package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.ApplicationLoggedInPayload
import org.malachite.estella.aplication.domain.ApplicationNoUserPayload
import org.malachite.estella.services.ApplicationService
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
    @Autowired private val securityService: SecurityService
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
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload): ResponseEntity<Void> {
        val saved = applicationService.insertApplicationWithoutUser(offerId, applicationPayload)
        return ResponseEntity.created(URI("/api/applications/" + saved.id)).build()
    }

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

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Any> {
        ex.printStackTrace()
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }


}
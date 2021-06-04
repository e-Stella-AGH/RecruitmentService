package demo.controllers

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.services.ApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/applications")
class ApplicationController(@Autowired private val applicationService: ApplicationService) {

    @PostMapping("/apply/{offerId}/user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationLoggedInPayload) =
        applicationService.insertApplicationLoggedInUser(offerId, applicationPayload)

    @PostMapping("/apply/{offerId}/no-user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload) =
        applicationService.insertApplicationWithoutUser(offerId, applicationPayload)
}
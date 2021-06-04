package demo.controllers

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.services.ApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.NoSuchElementException

@RestController
@RequestMapping("/applications")
class ApplicationController(@Autowired private val applicationService: ApplicationService) {
    @PostMapping("/apply/{offerId}/user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationLoggedInPayload) =
        applicationService.insertApplicationLoggedInUser(offerId, applicationPayload)

    @PostMapping("/apply/{offerId}/no-user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload) =
        applicationService.insertApplicationWithoutUser(offerId, applicationPayload)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(): ResponseEntity<Any> {
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }
}
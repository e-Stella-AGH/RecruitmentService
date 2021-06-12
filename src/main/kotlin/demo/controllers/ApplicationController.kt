package demo.controllers

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.services.ApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.NoSuchElementException

@RestController
@Transactional
@RequestMapping("/applications")
class ApplicationController(@Autowired private val applicationService: ApplicationService) {

    @CrossOrigin
    @PostMapping("/apply/{offerId}/user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationLoggedInPayload) =
        applicationService.insertApplicationLoggedInUser(offerId, applicationPayload)

    @CrossOrigin
    @PostMapping("/apply/{offerId}/no-user")
    fun applyForAnOffer(@PathVariable offerId: Int, @RequestBody applicationPayload: ApplicationNoUserPayload) =
        applicationService.insertApplicationWithoutUser(offerId, applicationPayload)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Any> {
        ex.printStackTrace()
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }


}
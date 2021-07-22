package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApplicationExceptionHandler {
    @ExceptionHandler(ApplicationNotFoundException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("We couldn't find this application")

}
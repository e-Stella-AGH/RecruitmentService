package org.malachite.estella.interview.api

import org.malachite.estella.commons.Message
import org.malachite.estella.interview.domain.InterviewNotFoundException
import org.malachite.estella.interview.domain.InvalidUUIDException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class InterviewExceptionHandler {

    @ExceptionHandler(InterviewNotFoundException::class)
    fun handleNoSuchElementException(ex: InterviewNotFoundException): ResponseEntity<Any> =
            ResponseEntity(Message("We couldn't find this interview"), HttpStatus.NOT_FOUND)

    @ExceptionHandler(InvalidUUIDException::class)
    fun handleIllegalArgument(ex: InvalidUUIDException): ResponseEntity<Any> =
            ResponseEntity(Message("Invalid UUID"), HttpStatus.BAD_REQUEST)
}
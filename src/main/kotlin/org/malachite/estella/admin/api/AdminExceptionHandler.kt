package org.malachite.estella.admin.api

import org.malachite.estella.commons.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AdminExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException() =
        ResponseEntity(Message("No object with provided UUID"), HttpStatus.BAD_REQUEST)

}
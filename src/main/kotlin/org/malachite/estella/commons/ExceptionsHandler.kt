package org.malachite.estella.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionsHandler {

    @ExceptionHandler(UnauthenticatedException::class)
    fun returnUnauthenticatedMessage(ex: UnauthenticatedException) =
        ResponseEntity(UnauthenticatedMessage, HttpStatus.UNAUTHORIZED)

    @ExceptionHandler(DataViolationException::class)
    fun returnDataViolationException(ex: DataViolationException) =
        ResponseEntity.badRequest().body(Message(ex.msg))

}
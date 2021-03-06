package org.malachite.estella.commons

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionsHandler {

    @ExceptionHandler(UnauthenticatedException::class)
    fun returnUnauthenticatedMessage(ex: UnauthenticatedException) =
        OwnResponses.UNAUTH

    @ExceptionHandler(DataViolationException::class)
    fun returnDataViolationException(ex: DataViolationException) =
        ResponseEntity.badRequest().body(Message(ex.msg))

    @ExceptionHandler(BadParamsException::class)
    fun returnBadParamsException(ex: BadParamsException) =
        ResponseEntity.badRequest().body(Message(ex.msg))

}
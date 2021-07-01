package org.malachite.estella.people.api

import org.malachite.estella.commons.Message
import org.malachite.estella.people.domain.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun returnBadRequestWhenEmailIsAlreadyInUse(ex: UserAlreadyExistsException): ResponseEntity<Message> =
        ResponseEntity(Message("Address already in use!"), HttpStatus.BAD_REQUEST)
}
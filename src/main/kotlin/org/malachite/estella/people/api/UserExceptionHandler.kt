package org.malachite.estella.people.api

import org.malachite.estella.commons.Message
import org.malachite.estella.people.domain.InvalidUserException
import org.malachite.estella.people.domain.UserAlreadyExistsException
import org.malachite.estella.people.domain.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun returnBadRequestWhenEmailIsAlreadyInUse(ex: UserAlreadyExistsException): ResponseEntity<Message> =
        ResponseEntity(Message("Address already in use!"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(UserNotFoundException::class)
    fun returnBadRequestWhenUserWasntFound(ex: UserNotFoundException) =
        ResponseEntity(Message("There is no such user"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(InvalidUserException::class)
    fun returnBadRequestWhenUserWasInvalid(ex: InvalidUserException) =
        ResponseEntity(Message("This user isn't job seeker, hr employee, nor organizaiton"), HttpStatus.BAD_REQUEST)
}
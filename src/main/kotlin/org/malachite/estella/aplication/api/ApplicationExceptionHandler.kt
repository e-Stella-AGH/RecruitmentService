package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.NotSpecifiedWhichNoteGet
import org.malachite.estella.aplication.domain.NoteNotAttachedException
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApplicationExceptionHandler {
    @ExceptionHandler(ApplicationNotFoundException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("We couldn't find this application")
}
@ControllerAdvice
class NotePostExceptionHandler {
    @ExceptionHandler(NoteNotAttachedException::class)
    fun handleNoSuchElementException(ex: NoteNotAttachedException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("You don't pass parameter where attach note")
}
@ControllerAdvice
class NoteGetExceptionHandler {
    @ExceptionHandler(NotSpecifiedWhichNoteGet::class)
    fun handleNoSuchElementException(ex: NotSpecifiedWhichNoteGet): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("You don't specify which type of notes should be returned")
}
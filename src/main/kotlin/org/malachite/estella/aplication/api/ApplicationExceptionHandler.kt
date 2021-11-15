package org.malachite.estella.aplication.api

import org.malachite.estella.aplication.domain.ApplicationNotFoundException
import org.malachite.estella.aplication.domain.NotSpecifiedWhichNoteToGet
import org.malachite.estella.aplication.domain.NoteNotAttachedException
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.task.domain.TaskResultNotExistException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApplicationExceptionHandler {
    @ExceptionHandler(ApplicationNotFoundException::class)
    fun handleApplicationNotFoundException(ex: ApplicationNotFoundException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("We couldn't find this application")
}
@ControllerAdvice
class NotePostExceptionHandler {
    @ExceptionHandler(NoteNotAttachedException::class)
    fun handleNoteNotAttachedException(ex: NoteNotAttachedException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("You haven't specified where to attach note")
}
@ControllerAdvice
class NoteGetExceptionHandler {
    @ExceptionHandler(NotSpecifiedWhichNoteToGet::class)
    fun handleNotSpecifiedWhichNoteToGet(ex: NotSpecifiedWhichNoteToGet): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("You haven't specified which type of notes should be returned")
}

@ControllerAdvice
class TaskResultNotExistExceptionHandler {
    @ExceptionHandler(TaskResultNotExistException::class)
    fun handleTaskResultNotExistException(ex: TaskResultNotExistException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("There is no task result for this task stage")
}
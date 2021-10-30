package org.malachite.estella.task.api

import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.task.domain.InvalidTestFileException
import org.malachite.estella.task.domain.TaskNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class TaskExceptionHandler {
    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(ex: TaskNotFoundException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("This task doesn't exist or you don't have access to it")

    @ExceptionHandler(InvalidTestFileException::class)
    fun handleInvalidTestFile(ex: InvalidTestFileException): ResponseEntity<Message> =
        ResponseEntity(Message("You provided malformed test file"), HttpStatus.BAD_REQUEST)
}
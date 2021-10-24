package org.malachite.estella.process.api

import org.malachite.estella.commons.Message
import org.malachite.estella.process.domain.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ProcessExceptionHandler {

    @ExceptionHandler(NoSuchStageTypeException::class)
    fun handleNoSuchStageTypeException(ex: NoSuchStageTypeException) =
        ResponseEntity.badRequest().body(Message("There's no such stage type as: ${ex.type}"))

    @ExceptionHandler(InvalidStagesListException::class)
    fun handleInvalidStagesListException(ex: InvalidStagesListException) =
        ResponseEntity.badRequest().body(Message(ex.error))

    @ExceptionHandler(InvalidDateException::class)
    fun handleInvalidEndDateException(ex: InvalidDateException) =
        ResponseEntity.badRequest().body(Message("Date you provided validates logic (either before now or before start of process)!"))

    @ExceptionHandler(ProcessAlreadyStartedException::class)
    fun handleProcessAlreadyStartedExcception(ex: ProcessAlreadyStartedException) =
        ResponseEntity.badRequest().body(Message(ex.message!!))

    @ExceptionHandler(ProcessNotFoundException::class)
    fun handleProcessNotFoundException(ex: ProcessNotFoundException) =
        ResponseEntity.badRequest().body(Message("Process wasn't found!"))

    @ExceptionHandler(ProcessNotStartedException::class)
    fun handleProcessNotStartedException(ex: ProcessNotStartedException) =
        ResponseEntity.badRequest().body(Message(ex.message!!))
}
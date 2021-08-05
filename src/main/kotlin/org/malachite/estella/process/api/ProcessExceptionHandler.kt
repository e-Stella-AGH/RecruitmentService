package org.malachite.estella.process.api

import org.malachite.estella.commons.Message
import org.malachite.estella.process.domain.InvalidStagesListException
import org.malachite.estella.process.domain.NoSuchStageTypeException
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
}
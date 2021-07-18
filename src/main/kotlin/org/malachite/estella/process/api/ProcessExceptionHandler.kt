package org.malachite.estella.process.api

import org.malachite.estella.commons.Message
import org.malachite.estella.process.domain.NoSuchStageTypeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ProcessExceptionHandler {

    @ExceptionHandler(NoSuchStageTypeException::class)
    fun handleNoSuchStageTypeException(ex: NoSuchStageTypeException) =
        ResponseEntity(Message("There's no such stage type as: ${ex.type}"), HttpStatus.BAD_REQUEST)
}
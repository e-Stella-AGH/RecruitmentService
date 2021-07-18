package org.malachite.estella.organization.api

import org.malachite.estella.commons.Message
import org.malachite.estella.organization.domain.OrganizationNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class OrganizationExceptionHandler {

    @ExceptionHandler(OrganizationNotFoundException::class)
    fun handleNoSuchElementException(ex: OrganizationNotFoundException): ResponseEntity<Any> =
        ResponseEntity.badRequest().body(Message("No organization with such id"))
}
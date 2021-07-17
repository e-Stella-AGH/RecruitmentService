package org.malachite.estella.offer.api

import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class OfferExceptionHandler {

    @ExceptionHandler(OfferNotFoundException::class)
    fun handleNoSuchElementException(ex: OfferNotFoundException): ResponseEntity<Any> =
        ResponseEntity("We couldn't find this offer", HttpStatus.NOT_FOUND)

    @ExceptionHandler(UnauthenticatedException::class)
    fun handleUnauthenticated(ex: UnauthenticatedException): ResponseEntity<Any> =
        OwnResponses.UNAUTH
}
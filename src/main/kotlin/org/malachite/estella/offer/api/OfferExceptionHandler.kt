package org.malachite.estella.offer.api

import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.offer.domain.ApplicationAlreadyMadeOnOfferException
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class OfferExceptionHandler {

    @ExceptionHandler(OfferNotFoundException::class)
    fun handleNoSuchElementException(ex: OfferNotFoundException): ResponseEntity<Message> =
        OwnResponses.NO_RESOURCE("We couldn't find this offer")

    @ExceptionHandler(UnauthenticatedException::class)
    fun handleUnauthenticated(ex: UnauthenticatedException): ResponseEntity<Message> =
        OwnResponses.UNAUTH

    @ExceptionHandler(ApplicationAlreadyMadeOnOfferException::class)
    fun handleUnauthenticated(ex: ApplicationAlreadyMadeOnOfferException): ResponseEntity<Any> =
        OwnResponses.BAD_REQUEST("Someone already applied on this offer!")
}
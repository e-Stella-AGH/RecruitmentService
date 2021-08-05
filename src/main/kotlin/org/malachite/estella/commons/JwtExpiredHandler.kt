package org.malachite.estella.commons

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class JwtExpiredHandler {

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleNoSuchElementException(ex: ExpiredJwtException): ResponseEntity<Message> =
        ResponseEntity(Message("Your token is expired please refresh it or login again"), HttpStatus.UNAUTHORIZED)

}
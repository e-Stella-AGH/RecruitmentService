package org.malachite.estella.commons.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
// - ogólnie ten handler brał dosłownie każdy wyjątek i nie pozwalał innym kontrolerom się w ogóle odpalić - na razie komentuję
// potem możemy pomyśleć, co z nim zrobić
//@ControllerAdvice
//class ControllerExceptionHandler {
//    @ExceptionHandler(Exception::class)
//    fun handleException(ex: Exception): ResponseEntity<Any> {
//        ex.printStackTrace()
//        return ResponseEntity("Unknown error occurred", HttpStatus.BAD_REQUEST)
//    }
//}
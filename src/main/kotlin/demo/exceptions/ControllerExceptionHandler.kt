package demo.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(): ResponseEntity<Any> {
        return ResponseEntity("Unknown error occurred", HttpStatus.BAD_REQUEST)
    }
}
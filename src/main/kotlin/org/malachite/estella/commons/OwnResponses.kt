package org.malachite.estella.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object OwnResponses{
    val UNAUTH: ResponseEntity<Message> = ResponseEntity(Message("Unauthenticated"),HttpStatus.BAD_REQUEST)
    val SUCCESS: ResponseEntity<Message> = ResponseEntity(SuccessMessage, HttpStatus.OK)
    val NO_RESOURCE: ResponseEntity<Message> = ResponseEntity(Message("No resource with such id"), HttpStatus.BAD_REQUEST)
}

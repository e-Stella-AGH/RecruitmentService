package org.malachite.estella.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


object OwnResponses{
    val UNAUTH: ResponseEntity<Message> = ResponseEntity(UnauthenticatedMessage,HttpStatus.UNAUTHORIZED)
    val SUCCESS: ResponseEntity<Message> = ResponseEntity(SuccessMessage, HttpStatus.OK)
    val NO_RESOURCE: ResponseEntity<Message> = ResponseEntity(NoResourceMessage, HttpStatus.BAD_REQUEST)
    fun <T>CREATED(elem:T):ResponseEntity<T> = ResponseEntity(elem,HttpStatus.CREATED)
    fun NO_RESOURCE(msg:String):ResponseEntity<Message> = ResponseEntity(Message(msg),HttpStatus.NOT_FOUND)
    fun BAD_REQUEST(msg:String):ResponseEntity<Any> = ResponseEntity(Message(msg),HttpStatus.BAD_REQUEST)
}




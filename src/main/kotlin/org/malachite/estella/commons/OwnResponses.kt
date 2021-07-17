package org.malachite.estella.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


object OwnResponses{
    val UNAUTH: ResponseEntity<Any> = ResponseEntity(UnauthenticatedMessage,HttpStatus.UNAUTHORIZED)
    val SUCCESS: ResponseEntity<Any> = ResponseEntity(SuccessMessage, HttpStatus.OK)
    val NO_RESOURCE: ResponseEntity<Any> = ResponseEntity(NoResourceMessage, HttpStatus.BAD_REQUEST)
    fun <T>CREATED(elem:T):ResponseEntity<T> = ResponseEntity(elem,HttpStatus.CREATED)
}




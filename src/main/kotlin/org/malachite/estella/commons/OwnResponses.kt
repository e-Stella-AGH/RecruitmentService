package org.malachite.estella.commons

import org.springframework.http.ResponseEntity

object OwnResponses{
    val UNAUTH: ResponseEntity<String> = ResponseEntity.status(401).body("Unauthenticated")
}

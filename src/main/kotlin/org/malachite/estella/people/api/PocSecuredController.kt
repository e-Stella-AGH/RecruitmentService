package org.malachite.estella.people.api

import org.malachite.estella.security.UserContextDetails
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/secured")
class PocSecuredController {
    @CrossOrigin
    @GetMapping("get")
    fun getSomething(): ResponseEntity<String> {
        UserContextDetails.fromContext()?.user?.let {
            println(it.firstName)
            println(it.lastName)
            println(it.mail)
        }
        return ResponseEntity.ok("Something")
    }
}

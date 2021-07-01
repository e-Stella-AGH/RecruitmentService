package org.malachite.estella.commons

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/_meta")
class Meta {

    @GetMapping("/health")
    fun health(): ResponseEntity<HealthDto> =
        ResponseEntity(HealthDto("App works"), HttpStatus.OK)

    data class HealthDto(
        val text: String
    )
}
package org.malachite.estella.admin.api

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.services.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/_admin")
class AdminController(
    @Autowired val organizationService: OrganizationService
) {

    @PostMapping("/verify/{organizationUUID}")
    fun verifyOrganization(@PathVariable organizationUUID: String): ResponseEntity<OrganizationDto> =
        organizationService
            .verifyOrganization(organizationUUID)
            .let { ResponseEntity(it.toDto(), HttpStatus.OK) }

    @PostMapping("/deverify/{organizationUUID}")
    fun deverifyOrganization(@PathVariable organizationUUID: String): ResponseEntity<OrganizationDto> =
        organizationService
            .deverifyOrganization(organizationUUID)
            .let { ResponseEntity(it.toDto(), HttpStatus.OK) }


    fun Organization.toDto() = OrganizationDto(
        this.id.toString(),
        this.name,
        this.verified ?: false
    )

    data class OrganizationDto(
        val uuid: String,
        val name: String,
        val verified: Boolean
    )
}
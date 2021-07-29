package org.malachite.estella.admin.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses.SUCCESS
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/_admin")
class AdminController(
    @Autowired val organizationService: OrganizationService,
    @Autowired val securityService: SecurityService
) {

    @PostMapping("/verify/{organizationUUID}")
    fun verifyOrganization(
        @RequestHeader(EStellaHeaders.adminApiKey) apiKey: String?,
        @PathVariable organizationUUID: String): ResponseEntity<Any> =
        if(securityService.isCorrectApiKey(apiKey))
            organizationService
                .verifyOrganization(organizationUUID)
                .let { SUCCESS }
        else
            UNAUTH


    @PostMapping("/deverify/{organizationUUID}")
    fun deverifyOrganization(
        @RequestHeader(EStellaHeaders.adminApiKey) apiKey: String?,
        @PathVariable organizationUUID: String): ResponseEntity<Any> =
        if(securityService.isCorrectApiKey(apiKey))
            organizationService
                .deverifyOrganization(organizationUUID)
                .let { SUCCESS }
        else
            UNAUTH

    data class OrganizationDto(
        val uuid: String,
        val name: String,
        val verified: Boolean
    )
}
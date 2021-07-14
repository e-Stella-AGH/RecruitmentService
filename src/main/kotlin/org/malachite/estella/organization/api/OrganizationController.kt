package org.malachite.estella.organization.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.HrPartnerResponse
import org.malachite.estella.people.domain.toResponse
import org.malachite.estella.services.HrPartnerService
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val hrPartnerService: HrPartnerService
) {

    @CrossOrigin
    @GetMapping
    fun getOrganizations(): ResponseEntity<MutableIterable<Organization>> {
        return ResponseEntity(organizationService.getOrganizations(), HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping("/{organizationId}")
    fun getOrganization(@PathVariable organizationId: OrganizationID): ResponseEntity<Organization> =
        ResponseEntity(organizationService.getOrganization(organizationId.toId()), HttpStatus.OK)


    @CrossOrigin
    @PostMapping()
    fun addOrganization(@RequestBody organization: OrganizationRequest): ResponseEntity<Organization> =
        organizationService.addOrganization(organization.toOrganization())
            .let { ResponseEntity(it,HttpStatus.CREATED) }

    @CrossOrigin
    @PutMapping("/{organizationId}")
    fun updateOrganization(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("organizationId") organizationId: OrganizationID,
        @RequestBody organization: OrganizationRequest
    ): ResponseEntity<Any> {
        if (!checkOrganizationUserRights(organizationId, jwt)) return OwnResponses.UNAUTH
        return organizationService.updateOrganization(organizationId.toId(), organization.toOrganization())
            .let { OwnResponses.SUCCESS }
    }

    @CrossOrigin
    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("organizationId") organizationId: OrganizationID
    ): ResponseEntity<Any> {
        if (!checkOrganizationUserRights(organizationId, jwt)) return OwnResponses.UNAUTH
        return organizationService.deleteOrganization(organizationId.toId()).let { OwnResponses.SUCCESS }
    }

    @CrossOrigin
    @GetMapping("/hrpartners")
    fun getHrPartners(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<MutableList<HrPartnerResponse>> {
        val organization = securityService.getOrganizationFromJWT(jwt)

        return hrPartnerService.getHrPartners()
            .filter { it.organization == organization }
            .map { it.toResponse() }
            .toMutableList().let { ResponseEntity(it, HttpStatus.OK) }
    }

    private fun checkOrganizationUserRights(organizationId: OrganizationID, jwt: String?): Boolean =
        organizationService.getOrganization(organizationId.toId())
            .let { securityService.checkUserRights(jwt, it.user.id!!) }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(): ResponseEntity<Any> {
        return OwnResponses.NO_RESOURCE
    }

    fun OrganizationRequest.toOrganization() = Organization(
        null, name,
        User(null, name, "", mail, password), false
    )

    fun OrganizationID.toId(): UUID = UUID.fromString(organizationId)
}


data class OrganizationRequest(val name: String, val mail: String, val password: String)
data class OrganizationID(val organizationId: String)

package org.malachite.estella.organization.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
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
    @Autowired private val securityService: SecurityService
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
    @PostMapping("/addorganization")
    fun addOrganization(@RequestBody organization: OrganizationRequest): ResponseEntity<Organization> =
        organizationService.addOrganization(organization.toOrganization())
            .let {
                ResponseEntity.created(URI("/api/organizations/" + it.id)).body(it)
            }

    @CrossOrigin
    @PutMapping("/{organizationId}")
    fun updateOrganization(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("organizationId") organizationId: OrganizationID,
        @RequestBody organization: OrganizationRequest
    ): ResponseEntity<Message> {
        if (!checkOrganizationUserRights(organizationId,jwt)) return OwnResponses.UNAUTH
        return organizationService.updateOrganization(organizationId.toId(), organization.toOrganization())
            .let { OwnResponses.SUCCESS }
    }

    @CrossOrigin
    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("organizationId") organizationId: OrganizationID
    ): ResponseEntity<Message> {
        if (!checkOrganizationUserRights(organizationId,jwt)) return OwnResponses.UNAUTH
        return organizationService.deleteOrganization(organizationId.toId()).let { OwnResponses.SUCCESS }
    }

    private fun checkOrganizationUserRights(organizationId: OrganizationID, jwt:String?):Boolean =
        organizationService.getOrganization(organizationId.toId())
            .let { securityService.checkUserRights(jwt, it.user.id!!) }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(): ResponseEntity<Message> {
        return OwnResponses.NO_RESOURCE
    }

    fun OrganizationRequest.toOrganization() = Organization(
        null, name,
        User(null, name, "", email, password), verified
    )

    fun OrganizationID.toId(): UUID = UUID.fromString(organizationId)
}


data class OrganizationRequest(val name: String, val email: String, val password: String, val verified: Boolean = false)
data class OrganizationID(val organizationId: String)

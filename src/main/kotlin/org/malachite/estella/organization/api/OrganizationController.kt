package org.malachite.estella.organization.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.HrPartnerResponse
import org.malachite.estella.people.domain.toResponse
import org.malachite.estella.services.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val offerService: OfferService
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
    @GetMapping("/organization")
    fun getOrganizationByUser(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<Any> =
        securityService.getOrganizationFromJWT(jwt)
            ?.let { ResponseEntity.ok(it) }
            ?: UNAUTH

    @CrossOrigin
    @GetMapping("/offers")
    fun getOrganizationsOffers(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<Any> =
        securityService.getOrganizationFromJWT(jwt)
            ?.let { offerService.getOrganizationOffers(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: UNAUTH


    @CrossOrigin
    @PostMapping()
    fun addOrganization(@RequestBody organizationRequest: OrganizationRequest): ResponseEntity<Organization> =
        organizationService.addOrganization(organizationRequest.toOrganization())
            .let { OwnResponses.CREATED(it) }

    @CrossOrigin
    @PutMapping("/{organizationId}")
    fun updateOrganization(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("organizationId") organizationId: OrganizationID,
        @RequestBody organizationRequest: OrganizationRequest
    ): ResponseEntity<Any> {
        if (!checkOrganizationUserRights(organizationId, jwt)) return OwnResponses.UNAUTH
        return organizationService.updateOrganization(organizationId.toId(), organizationRequest.toOrganization())
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
    fun getHrPartners(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<Any> =
        securityService.getOrganizationFromJWT(jwt)
            ?.let{
            hrPartnerService.getHrPartners()
                .filter { hr -> hr.organization == it }
                .map { it.toResponse() }
                .toMutableList()
                .let { ResponseEntity.ok(it) }
        }
            ?: UNAUTH



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

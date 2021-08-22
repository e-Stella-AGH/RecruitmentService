package org.malachite.estella.organization.api

import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.OwnResponses.UNAUTH
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.HrPartnerResponse
import org.malachite.estella.people.domain.toResponse
import org.malachite.estella.services.HrPartnerService
import org.malachite.estella.services.OfferService
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.SecurityService
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
    fun getOrganizationByUser(): ResponseEntity<Any> =
        securityService.getOrganizationFromContext()
            ?.let { ResponseEntity.ok(it) }
            ?: UNAUTH

    @CrossOrigin
    @GetMapping("/offers")
    fun getOrganizationsOffers(): ResponseEntity<Any> =
        securityService.getOrganizationFromContext()
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
        @PathVariable("organizationId") organizationId: OrganizationID,
        @RequestBody organizationRequest: OrganizationRequest
    ): ResponseEntity<Any> =
        organizationService.updateOrganization(organizationId.toId(), organizationRequest.toOrganization())
            .let { OwnResponses.SUCCESS }


    @CrossOrigin
    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(
        @PathVariable("organizationId") organizationId: OrganizationID
    ): ResponseEntity<Any> =
        organizationService.deleteOrganization(organizationId.toId()).let { OwnResponses.SUCCESS }



    @CrossOrigin
    @GetMapping("/hrpartners")
    fun getHrPartners(): ResponseEntity<List<HrPartnerResponse>> =
        hrPartnerService.getHrPartners()
            .filter { it.organization == securityService.getOrganizationFromContext() }
            .map { it.toResponse() }
            .toList()
            .let { ResponseEntity.ok(it) }


    fun OrganizationRequest.toOrganization() = Organization(
        null, name,
        User(null, name, "", mail, password), false
    )

    fun OrganizationID.toId(): UUID = UUID.fromString(organizationId)
}


data class OrganizationRequest(val name: String, val mail: String, val password: String)
data class OrganizationID(val organizationId: String)

package org.malachite.estella.people.api

import org.malachite.estella.commons.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import org.malachite.estella.services.HrPartnerService
import org.malachite.estella.services.OfferService
import org.malachite.estella.services.OrganizationService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/hrpartners")
class HrPartnerController(
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val offerService: OfferService
) {
    @CrossOrigin
    @GetMapping
    fun getHrPartners(): ResponseEntity<MutableIterable<HrPartner>> {
        return ResponseEntity.ok(hrPartnerService.getHrPartners())
    }

    @CrossOrigin
    @GetMapping("/{hrPartnerId}")
    fun getHrPartner(@PathVariable("hrPartnerId") hrPartnerId: Int): ResponseEntity<HrPartner> {
        val partner: HrPartner = hrPartnerService.getHrPartner(hrPartnerId)

        return ResponseEntity.ok(partner)
    }

    @CrossOrigin
    @GetMapping("/offers")
    fun getHrPartnerOffers(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<MutableList<OfferResponse>> {
        val hrPartner = securityService.getHrPartnerFromJWT(jwt)
            ?: return ResponseEntity(mutableListOf(), HttpStatus.UNAUTHORIZED)

        return offerService.getOffers()
            .filter { offer -> offer.creator == hrPartner }
            .map { offer -> offer.toOfferResponse() }
            .toMutableList().let { ResponseEntity(it, HttpStatus.OK) }
    }

    @CrossOrigin
    @PostMapping()
    fun addHrPartner(
        @RequestBody hrPartnerRequest: HrPartnerRequest,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?
    ): ResponseEntity<Any>{
        val organizationUser = securityService.getUserFromJWT(jwt)
            ?: return OwnResponses.UNAUTH
        val organization = organizationService.getOrganizationByUser(organizationUser)
        return hrPartnerService
            .registerHrPartner(hrPartnerRequest.toHrPartner(organization))
            .let { OwnResponses.CREATED(it)}
    }

    @CrossOrigin
    @DeleteMapping("/{hrPartnerId}")
    fun deleteHrPartner(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("hrPartnerId") hrId: Int
    ): ResponseEntity<Any> {
        if (!securityService.checkHrRights(jwt, hrId)) return OwnResponses.UNAUTH
        return hrPartnerService.deleteHrPartner(hrId).let {
            OwnResponses.SUCCESS
        }
    }
}

data class HrPartnerRequest(val mail: String) {
    fun toHrPartner(organization: Organization): HrPartner =
        HrPartner(null, organization, User(null, "", "", mail))
}
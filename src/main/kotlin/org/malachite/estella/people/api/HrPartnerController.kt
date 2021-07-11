package org.malachite.estella.people.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
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
        return ResponseEntity(hrPartnerService.getHrPartners(), HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping("/{hrPartnerId}")
    fun getHrPartner(@PathVariable("hrPartnerId") hrPartnerId: Int): ResponseEntity<HrPartner> {
        val partner: HrPartner = hrPartnerService.getHrPartner(hrPartnerId)

        return ResponseEntity(partner, HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping("/offers")
    fun getHrPartnerOffers(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<MutableList<OfferResponse>> {
        val hrPartner = securityService.getHrPartnerFromJWT(jwt)
            ?: return ResponseEntity(mutableListOf(), HttpStatus.UNAUTHORIZED)

        return offerService.getOffers()
            .filter { offer -> offer.creator == hrPartner }
            .map { offer -> OfferResponse.fromOffer(offer) }
            .toMutableList().let { ResponseEntity(it, HttpStatus.OK) }
    }

    @CrossOrigin
    @PostMapping("/addHrPartner")
    fun addHrPartner(
        @RequestBody hrPartnerRequest: HrPartnerRequest,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?
    ): ResponseEntity<Message> {
        val organizationUser = securityService.getUserFromJWT(jwt)
            ?: return OwnResponses.UNAUTH
        val organization = organizationService.getOrganizationByUser(organizationUser)
        val saved: HrPartner = hrPartnerService.registerHrPartner(hrPartnerRequest.toHrPartner(organization))
        return ResponseEntity.created(URI("/api/hrpartners/" + saved.id)).body(SuccessMessage)
    }

    @CrossOrigin
    @DeleteMapping("/{hrPartnerId}")
    fun deleteHrPartner(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("hrPartnerId") hrId: Int
    ): ResponseEntity<Message> {
        if (!securityService.checkHrRights(jwt, hrId)) return OwnResponses.UNAUTH
        return hrPartnerService.deleteHrPartner(hrId).let {
            ResponseEntity(SuccessMessage, HttpStatus.OK)
        }
    }
}

data class HrPartnerRequest(val mail: String) {
    fun toHrPartner(organization: Organization): HrPartner =
        HrPartner(null, organization, User(null, "", "", mail))
}
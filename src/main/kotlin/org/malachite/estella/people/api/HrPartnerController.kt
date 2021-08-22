package org.malachite.estella.people.api

import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.services.HrPartnerService
import org.malachite.estella.services.OfferService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/hrpartners")
class HrPartnerController(
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val offerService: OfferService
) {
    @CrossOrigin
    @GetMapping
    fun getHrPartners(): ResponseEntity<List<HrPartner>> =
        ResponseEntity.ok(hrPartnerService.getHrPartners().toList())

    @CrossOrigin
    @GetMapping("/{hrPartnerId}")
    fun getHrPartner(@PathVariable("hrPartnerId") hrPartnerId: Int): ResponseEntity<HrPartner> =
        hrPartnerService.getHrPartner(hrPartnerId).let { ResponseEntity.ok(it) }

    @Transactional
    @CrossOrigin
    @GetMapping("/offers")
    fun getHrPartnerOffers(): ResponseEntity<List<OfferResponse>> {
        return offerService.getHrPartnerOffers()
            .let { ResponseEntity.ok(it) }
    }

    @CrossOrigin
    @PostMapping
    fun addHrPartner(
        @RequestBody hrPartnerRequest: HrPartnerRequest
    ): ResponseEntity<Any> {
        return hrPartnerService
            .registerHrPartner(hrPartnerRequest)
            .let { OwnResponses.CREATED(it) }
    }

    @CrossOrigin
    @DeleteMapping("/{hrPartnerId}")
    fun deleteHrPartner(
        @PathVariable("hrPartnerId") hrId: Int
    ): ResponseEntity<Any> =
        hrPartnerService.deleteHrPartner(hrId).let {
            OwnResponses.SUCCESS
        }

    @CrossOrigin
    @DeleteMapping("/mail")
    fun deleteHrPartnerByMail(
        @RequestBody mail: HrPartnerMail
    ): ResponseEntity<Any> =
        hrPartnerService.deleteHrPartnerByMail(mail.mail).let {
            OwnResponses.SUCCESS
        }
}

data class HrPartnerMail(val mail: String)

data class HrPartnerRequest(val firstName: String, val lastName: String, val mail: String) {
    fun toHrPartner(organization: Organization): HrPartner =
        HrPartner(null, organization, User(null, firstName, lastName, mail))
}
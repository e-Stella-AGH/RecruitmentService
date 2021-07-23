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
    fun getHrPartners(): ResponseEntity<MutableIterable<HrPartner>> =
        ResponseEntity.ok(hrPartnerService.getHrPartners())


    @CrossOrigin
    @GetMapping("/{hrPartnerId}")
    fun getHrPartner(@PathVariable("hrPartnerId") hrPartnerId: Int): ResponseEntity<HrPartner> =
        hrPartnerService.getHrPartner(hrPartnerId).let { ResponseEntity.ok(it) }


    @CrossOrigin
    @GetMapping("/offers")
    fun getHrPartnerOffers(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<List<OfferResponse>> =
        securityService.getHrPartnerFromJWT(jwt)
            ?.let { offerService.getHrPartnerOffers(it) }
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(mutableListOf(), HttpStatus.UNAUTHORIZED)


    @CrossOrigin
    @PostMapping()
    fun addHrPartner(
        @RequestBody hrPartnerRequest: HrPartnerRequest,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?
    ): ResponseEntity<Any> =
        securityService.getUserFromJWT(jwt)
            ?.let { organizationService.getOrganizationByUser(it) }
            ?.let { hrPartnerService.registerHrPartner(hrPartnerRequest.toHrPartner(it)) }
            ?.let { OwnResponses.CREATED(it) }
            ?: OwnResponses.UNAUTH

    @CrossOrigin
    @DeleteMapping("/{hrPartnerId}")
    fun deleteHrPartner(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("hrPartnerId") hrId: Int
    ): ResponseEntity<Any> =
        if (!securityService.checkHrRights(jwt, hrId)) OwnResponses.UNAUTH
        else hrPartnerService.deleteHrPartner(hrId).let { OwnResponses.SUCCESS }

    @CrossOrigin
    @DeleteMapping("/mail")
    fun deleteHrPartnerByMail(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestHeader(EStellaHeaders.hrPartnerMail) mail: String?
    ): ResponseEntity<Any> =
        securityService.getOrganizationFromJWT(jwt)?.let {
            hrPartnerService.getHrPartnerByMail(mail).let {
                if (!securityService.checkOrganizationHrRights(jwt, it!!.user.id!!)) OwnResponses.UNAUTH
                else {
                    hrPartnerService.deleteHrPartner(it.id!!)
                    OwnResponses.SUCCESS
                }
            }
        }
            ?: OwnResponses.UNAUTH
}

data class HrPartnerRequest(val firstName: String, val lastName: String, val mail: String) {
    fun toHrPartner(organization: Organization): HrPartner =
        HrPartner(null, organization, User(null, firstName, lastName, mail))
}
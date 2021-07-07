package org.malachite.estella.offer.api

import org.malachite.estella.commons.*
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.offer.domain.OfferRequest
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import org.malachite.estella.services.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.sql.Date
import java.time.LocalDate
import java.util.*

@RestController
@Transactional
@RequestMapping("/api/offers")
class OfferController(
    @Autowired private val offerService: OfferService,
    @Autowired private val securityService: SecurityService
) {

    @CrossOrigin
    @GetMapping
    fun getOffers(): ResponseEntity<List<OfferResponse>> =
        offerService.getOffers()
            .map { it.toOfferResponse() }
            .let { ResponseEntity(it, HttpStatus.OK) }


    @CrossOrigin
    @GetMapping("/{offerId}")
    fun getOffer(@PathVariable offerId: Int): ResponseEntity<OfferResponse> =
        offerService.getOffer(offerId)
            .let { ResponseEntity(it.toOfferResponse(), HttpStatus.OK) }

    @CrossOrigin
    @PostMapping("/addoffer")
    fun addOffer(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody offer: OfferRequest
    ): ResponseEntity<Message> {
        val hrPartner = getHrPartnerFromJWT(jwt)
        offerService.addOffer(offer, hrPartner)
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

    @CrossOrigin
    @PutMapping("/update/{offerId}")
    fun updateOffer(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("offerId") offerId: Int,
        @RequestBody offer: OfferRequest
    ): ResponseEntity<Any> {
        val hrPartner = getHrPartnerFromJWT(jwt)
        offerService.updateOffer(offerId, offer, hrPartner)
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

    @CrossOrigin
    @DeleteMapping("/{offerId}")
    fun deleteOffer(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("offerId") offerId: Int
    ): ResponseEntity<Message> {
        offerService.deleteOffer(offerId)
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

    private fun getHrPartnerFromJWT(jwt: String?) =
        securityService.getHrPartnerFromJWT(jwt) ?: throw UnauthenticatedException()

}
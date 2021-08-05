package org.malachite.estella.offer.api

import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.offer.domain.OfferRequest
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import org.malachite.estella.services.OfferService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Transactional
@RequestMapping("/api/offers")
class OfferController(
    @Autowired private val offerService: OfferService
) {

    @CrossOrigin
    @GetMapping
    fun getOffers(): ResponseEntity<List<OfferResponse>> =
        offerService.getOffers()
            .map { it.toOfferResponse() }
            .let { ResponseEntity.ok(it) }


    @CrossOrigin
    @GetMapping("/{offerId}")
    fun getOffer(@PathVariable offerId: Int): ResponseEntity<OfferResponse> =
        offerService.getOffer(offerId)
            .let { ResponseEntity.ok(it.toOfferResponse()) }

    @CrossOrigin
    @PostMapping()
    fun addOffer(
        @RequestBody offerRequest: OfferRequest
    ): ResponseEntity<OfferResponse> =
        offerService.addOffer(offerRequest)
            .let(Offer::toOfferResponse)
            .let { OwnResponses.CREATED(it) }

    @CrossOrigin
    @PutMapping("/{offerId}")
    fun updateOffer(
        @PathVariable("offerId") offerId: Int,
        @RequestBody offerRequest: OfferRequest
    ): ResponseEntity<Any> =
        offerService.updateOffer(offerId, offerRequest)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @DeleteMapping("/{offerId}")
    fun deleteOffer(
        @PathVariable("offerId") offerId: Int
    ): ResponseEntity<Any> =
        offerService.deleteOffer(offerId)
            .let { OwnResponses.SUCCESS }
}
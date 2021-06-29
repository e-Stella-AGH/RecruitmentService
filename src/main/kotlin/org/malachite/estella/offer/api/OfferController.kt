package org.malachite.estella.offer.api

import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.offer.domain.OfferRequest
import org.malachite.estella.offer.domain.OfferResponse
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
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val securityService: SecurityService
) {

    @CrossOrigin
    @GetMapping
    fun getOffers(): ResponseEntity<List<OfferResponse>> {
        return ResponseEntity(offerService.getOffers().map { OfferResponse.fromOffer(it) }, HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping("/{offerId}")
    fun getOffer(@PathVariable offerId: Int): ResponseEntity<OfferResponse> {
        val offer = offerService.getOffer(offerId)

        return ResponseEntity(OfferResponse.fromOffer(offer), HttpStatus.OK)
    }

    @CrossOrigin
    @PostMapping("/addoffer")
    fun addOffer(@RequestBody offer: OfferRequest, @CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        val hrPartner =
            securityService.getHrPartnerFromJWT(jwt) ?: return ResponseEntity.status(404).body("Unauthenticated")

        val saved: Offer = offerService.addOffer(offer.toOffer(hrPartner, desiredSkillService))

        val recruitmentProcess = RecruitmentProcess(
            null,
            Date.valueOf(LocalDate.now()),
            null,
            saved,
            listOf(RecruitmentStage(null, StageType.APPLIED)),
            setOf(), setOf()
        )
        recruitmentProcessService.addProcess(recruitmentProcess)

        return ResponseEntity.created(URI("/api/offers/" + saved.id)).build()
    }

    @CrossOrigin
    @PutMapping("/update/{offerId}")
    fun updateOffer(@PathVariable("offerId") offerId: Int, @RequestBody offer: OfferRequest,
                    @CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        val hrPartner =
            securityService.getHrPartnerFromJWT(jwt) ?: return ResponseEntity.status(404).body("Unauthenticated")
        offerService.updateOffer(offerId, offer.toOffer(hrPartner, desiredSkillService))
        return ResponseEntity(HttpStatus.OK)
    }

    @CrossOrigin
    @DeleteMapping("/{offerId}")
    fun deleteOffer(@PathVariable("offerId") offerId: Int): ResponseEntity<Offer> {
        offerService.deleteOffer(offerId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(): ResponseEntity<Any> {
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }

}
package demo.controllers

import demo.models.offers.DesiredSkill
import demo.models.offers.Offer
import demo.models.offers.SkillLevel
import demo.services.DesiredSkillService
import demo.services.HrPartnerService
import demo.services.OfferService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.sql.Clob
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashSet

@RestController
@RequestMapping("/api/offers")
class OfferController(@Autowired private val offerService: OfferService,
                      @Autowired private val hrPartnerService: HrPartnerService,
                      @Autowired private val desiredSkillService: DesiredSkillService) {

    @GetMapping()
    fun getOffers(): ResponseEntity<MutableIterable<Offer>> {
        return ResponseEntity(offerService.getOffers(), HttpStatus.OK)
    }

    @GetMapping("/{offerId}")
    fun getOffer(@PathVariable offerId: Int): ResponseEntity<OfferResponse> {
        val offer = offerService.getOffer(offerId)

        return ResponseEntity(OfferResponse.fromOffer(offer), HttpStatus.OK)
    }

    @PostMapping("/addoffer")
    fun addOffer(@RequestBody offer: OfferRequest): ResponseEntity<Offer> {
        val saved: Offer = offerService.addOffer(offer.toOffer(hrPartnerService, desiredSkillService))

        return ResponseEntity.created(URI("/api/offers/" + saved.id)).build()
    }

    @PutMapping("/update/{offerId}")
    fun updateOffer(@PathVariable("offerId") offerId: Int, @RequestBody offer: OfferRequest) : ResponseEntity<Offer> {
        offerService.updateOffer(offerId, offer.toOffer(hrPartnerService, desiredSkillService))
        return ResponseEntity(HttpStatus.OK)
    }

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

data class OfferRequest(
    val name:String, val description:String, val position:String,
    val minSalary:Long, val maxSalary:Long, val localization: String,
    val creatorId:Int, val skills: List<Skill>) {

    fun toOffer( hrPartnerService: HrPartnerService, desiredSkillService: DesiredSkillService): Offer {
        //TODO below is ugly af, must change after June 8th
        val skillSet: Set<DesiredSkill?> = toDesiredSkillSet(desiredSkillService)
        if (skillSet.filterNotNull().isEmpty()) {
            desiredSkillService.addDesiredSkills(skills.map { DesiredSkill(null, it.name, SkillLevel.valueOf(it.level)) })
        }
        val strongSkillSet: Set<DesiredSkill> = toDesiredSkillSet(desiredSkillService).filterNotNull().map { it!! }.toHashSet()

        return Offer(null, name,
            javax.sql.rowset.serial.SerialClob(description.toCharArray()), position, minSalary, maxSalary,
            localization, hrPartnerService.getHrPartner(creatorId), strongSkillSet, null)
    }



    private fun toDesiredSkillSet(desiredSkillService: DesiredSkillService) = skills
        .map { desiredSkillService
            .safeGetDesiredSkill(Pair(it.name, SkillLevel.valueOf(it.level))) }
        .toCollection(HashSet<DesiredSkill?>())
}

data class Skill(
    val name:String,
    val level:String
)

data class OfferResponse(val name:String, val description:String, val position:String,
                         val minSalary:Long, val maxSalary:Long, val localization: String,
                         val creatorId: Int, val skills: Set<DesiredSkill>) {
    companion object {
        fun fromOffer(offer: Offer): OfferResponse {
            return OfferResponse(offer.name, offer.description.characterStream.readText(),
                offer.position, offer.minSalary, offer.maxSalary, offer.localization, offer.creator.id!!, offer.skills)
        }
    }
}
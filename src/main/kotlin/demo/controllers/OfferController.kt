package demo.controllers

import demo.loader.FakeRecruitmentProcess
import demo.models.offers.*
import demo.repositories.RecruitmentProcessRepository
import demo.services.DesiredSkillService
import demo.services.HrPartnerService
import demo.services.OfferService
import demo.services.RecruitmentProcessService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.sql.Clob
import java.sql.Date
import java.time.LocalDate
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashSet

@RestController
@Transactional
@RequestMapping("/api/offers")
class OfferController(
    @Autowired private val offerService: OfferService,
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService
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
    fun addOffer(@RequestBody offer: OfferRequest): ResponseEntity<Offer> {
        val saved: Offer = offerService.addOffer(offer.toOffer(hrPartnerService, desiredSkillService))

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
    fun updateOffer(@PathVariable("offerId") offerId: Int, @RequestBody offer: OfferRequest): ResponseEntity<Offer> {
        offerService.updateOffer(offerId, offer.toOffer(hrPartnerService, desiredSkillService))
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

data class OfferRequest(
    val name: String, val description: String, val position: String,
    val minSalary: Long, val maxSalary: Long, val localization: String,
    val creatorId: Int, val skills: List<Skill>
) {

    fun toOffer(hrPartnerService: HrPartnerService, desiredSkillService: DesiredSkillService): Offer {
        //TODO below is ugly af, must change after June 8th
        val skillSet: Set<DesiredSkill?> = toDesiredSkillSet(desiredSkillService)
        if (skillSet.filterNotNull().isEmpty()) {
            desiredSkillService.addDesiredSkills(skills.map {
                DesiredSkill(
                    null,
                    it.name,
                    SkillLevel.valueOf(it.level)
                )
            })
        }
        val strongSkillSet: Set<DesiredSkill> =
            toDesiredSkillSet(desiredSkillService).filterNotNull().map { it!! }.toHashSet()



        return Offer(
            null, name,
            javax.sql.rowset.serial.SerialClob(description.toCharArray()), position, minSalary, maxSalary,
            localization, hrPartnerService.getHrPartner(creatorId), strongSkillSet, null
        )
    }


    private fun toDesiredSkillSet(desiredSkillService: DesiredSkillService) = skills
        .map {
            desiredSkillService
                .safeGetDesiredSkill(Pair(it.name, SkillLevel.valueOf(it.level)))
        }
        .toCollection(HashSet<DesiredSkill?>())
}

data class Skill(
    val name: String,
    val level: String
)

data class OfferResponse(
    val id: Int?, val name: String, val description: String, val position: String,
    val minSalary: Long, val maxSalary: Long, val localization: String,
    val organizationName: String, val skills: Set<DesiredSkill>
) {
    companion object {
        fun fromOffer(offer: Offer): OfferResponse {
            return OfferResponse(
                offer.id, offer.name, offer.description.characterStream.readText(),
                offer.position, offer.minSalary, offer.maxSalary, offer.localization, offer.creator.organization.name, offer.skills
            )
        }
    }
}
package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.UnauthenticatedMessage
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.malachite.estella.offer.domain.OfferRepository
import org.malachite.estella.offer.domain.OfferRequest
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.util.*
import kotlin.NoSuchElementException

@Service
class OfferService(
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
) : EStellaService() {

    override val throwable: Exception = OfferNotFoundException()

    fun getOffers(): MutableIterable<Offer> =
        offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = withExceptionThrower { offerRepository.findById(id).get() } as Offer

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> =
        getOffer(id).skills.toMutableSet()

    fun addOffer(offer: OfferRequest, hrPartner: HrPartner) {
        val saved: Offer = this.addOffer(offer.toOffer(hrPartner, desiredSkillService))
        recruitmentProcessService.addBasicProcess(saved)
    }

    fun addOffer(offer: Offer): Offer = withExceptionThrower { offerRepository.save(offer) } as Offer

    fun updateOffer(id: Int, offer: Offer) {
        val currOffer: Optional<Offer> = offerRepository.findById(id)
        if (currOffer.isPresent) {
            val updated: Offer = currOffer.get().copy(
                name = offer.name,
                description = offer.description,
                position = offer.position,
                minSalary = offer.minSalary,
                maxSalary = offer.maxSalary,
                localization = offer.localization,
                skills = offer.skills
            )
            offerRepository.save(updated)
        } else {
            throw OfferNotFoundException()
        }
    }

    fun updateOffer(id: Int, offer: OfferRequest, hrPartner: HrPartner) {
        this.updateOffer(id, offer.toOffer(hrPartner, desiredSkillService))
    }

    fun deleteOffer(id: Int) = offerRepository.deleteById(id)
}
package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.malachite.estella.offer.domain.OfferRepository
import org.malachite.estella.offer.domain.OfferRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OfferService(
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService
) : EStellaService() {

    override val throwable: Exception = OfferNotFoundException()

    fun getOffers(): MutableIterable<Offer> =
        offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = withExceptionThrower { offerRepository.findById(id).get() } as Offer

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> =
        getOffer(id).skills.toMutableSet()

    fun addOffer(offerRequest: OfferRequest, hrPartner: HrPartner):Offer =
        this.addOffer(offerRequest.toOffer(hrPartner, desiredSkillService))
            .also {  recruitmentProcessService.addBasicProcess(it) }

    fun addOffer(offer: Offer): Offer = offerRepository.save(offer)

    private fun updateOffer(oldOffer: Offer, newOffer: Offer) {
        val updated: Offer = oldOffer.copy(
            name = newOffer.name,
            description = newOffer.description,
            position = newOffer.position,
            minSalary = newOffer.minSalary,
            maxSalary = newOffer.maxSalary,
            localization = newOffer.localization,
            skills = newOffer.skills
        )
        offerRepository.save(updated)
    }

    fun updateOffer(id: Int, offerRequest: OfferRequest, hrPartner: HrPartner) {
        val currentOffer = this.getOffer(id)
        if(!checkHrPartnerRightsForOffer(currentOffer, hrPartner)) throw UnauthenticatedException()
        this.updateOffer(currentOffer, offerRequest.toOffer(hrPartner, desiredSkillService))
    }

    fun deleteOffer(id: Int, hrPartner: HrPartner) {
        val offer = this.getOffer(id)
        if(!checkHrPartnerRightsForOffer(offer, hrPartner)) throw UnauthenticatedException()
        deleteOffer(id)
    }

    private fun deleteOffer(id: Int) = offerRepository.deleteById(id)

    private fun checkHrPartnerRightsForOffer(offer: Offer, hrPartner: HrPartner) =
        offer.creator.id == hrPartner.id
}
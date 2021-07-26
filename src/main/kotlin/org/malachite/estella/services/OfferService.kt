package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.offer.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OfferService(
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
) : EStellaService<Offer>() {

    override val throwable: Exception = OfferNotFoundException()

    fun getOffers(): MutableIterable<Offer> =
        offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = withExceptionThrower { offerRepository.findById(id).get() }

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> =
        getOffer(id).skills.toMutableSet()

    fun addOffer(offerRequest: OfferRequest, hrPartner: HrPartner): Offer =
        this.addOffer(offerRequest.toOffer(hrPartner, desiredSkillService))
            .also { recruitmentProcessService.addBasicProcess(it) }

    fun addOffer(offer: Offer): Offer = offerRepository.save(offer)

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

    fun updateOffer(id: Int, offerRequest: OfferRequest, hrPartner: HrPartner) {
        this.updateOffer(id, offerRequest.toOffer(hrPartner, desiredSkillService))
    }

    fun getHrPartnerOffers(hrPartner: HrPartner): List<OfferResponse> = getOffers()
            .filter { it.creator == hrPartner }
            .map { it.toOfferResponse() }

    fun getOrganizationOffers(organization: Organization): List<OfferResponse> = getOffers()
        .filter { it.creator.organization == organization }
        .map { it.toOfferResponse() }

    fun deleteOffer(id: Int) = offerRepository.deleteById(id)
}
package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.offer.domain.*
import org.malachite.estella.security.Authority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OfferService(
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val desiredSkillService: DesiredSkillService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val securityService: SecurityService
) : EStellaService<Offer>() {

    override val throwable: Exception = OfferNotFoundException()

    fun getOffers(): MutableIterable<Offer> =
        offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = withExceptionThrower { offerRepository.findById(id).get() }

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> =
        getOffer(id).skills.toMutableSet()

    fun addOffer(offerRequest: OfferRequest): Offer {
        offerRequest.toOffer(
            securityService.getHrPartnerFromContext() ?: throw UnauthenticatedException(),
            desiredSkillService
        ).let {
            if (!checkAuth(it).contains(Permission.CREATE)) throw UnauthenticatedException()
            return this.addOffer(it)
                .also { recruitmentProcessService.addBasicProcess(it) }
        }
    }

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

    fun updateOffer(id: Int, offerRequest: OfferRequest) {
        val currentOffer = this.getOffer(id)
        if (!checkAuth(currentOffer).contains(Permission.UPDATE)) {
            throw UnauthenticatedException()
        }
        this.updateOffer(
            currentOffer,
            offerRequest.toOffer(
                currentOffer.creator,
                desiredSkillService
            )
        )
    }

    fun deleteOffer(id: Int) {
        if (!checkAuth(this.getOffer(id)).contains(Permission.DELETE)) throw UnauthenticatedException()
        offerRepository.deleteById(id)
    }

    fun getHrPartnerOffers(hrPartner: HrPartner): List<OfferResponse> = getOffers()
            .filter { it.creator == hrPartner }
            .map { it.toOfferResponse() }

    fun getOrganizationOffers(organization: Organization): List<OfferResponse> = getOffers()
        .filter { it.creator.organization == organization }
        .map { it.toOfferResponse() }

    private fun checkAuth(offer: Offer): Set<Permission> {
        val userDetails = securityService.getUserDetailsFromContext() ?: throw UnauthenticatedException()
        if (securityService.isCorrectApiKey(userDetails.token)) return Permission.allPermissions()
        val user = userDetails.user
        val userAuthority = userDetails.authorities.first()
        return when(userAuthority) {
            Authority.job_seeker ->
                throw UnauthenticatedException()
            Authority.hr ->
                if(offer.creator.user.id == user.id && offer.creator.organization.verified)
                    Permission.allPermissions()
                else
                    setOf(Permission.READ)
            Authority.organization ->
                if(offer.creator.organization.user.id == user.id && offer.creator.organization.verified)
                    setOf(Permission.READ, Permission.UPDATE, Permission.DELETE)
                else
                    setOf(Permission.READ)

        }
    }

    fun getHrPartnerOffers(): List<OfferResponse> =
        securityService.getUserDetailsFromContext()?.let { userDetails ->
            getOffers()
                .filter { userDetails.authorities.firstOrNull() == Authority.hr }
                .filter { it.creator.id == userDetails.user.id }
                .map { it.toOfferResponse() }
        } ?: listOf()
}
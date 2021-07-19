package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
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
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val securityService: SecurityService
) : EStellaService() {

    override val throwable: Exception = OfferNotFoundException()

    fun getOffers(): MutableIterable<Offer> =
        offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = withExceptionThrower { offerRepository.findById(id).get() } as Offer

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> =
        getOffer(id).skills.toMutableSet()

    fun addOffer(offerRequest: OfferRequest, jwt: String?): Offer {
        offerRequest.toOffer(
            securityService.getHrPartnerFromJWT(jwt) ?: throw UnauthenticatedException(),
            desiredSkillService
        ).let {
            if (!checkAuth(it, jwt).contains(Permission.CREATE)) throw UnauthenticatedException()
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

    fun updateOffer(id: Int, offerRequest: OfferRequest, jwt: String?) {
        val currentOffer = this.getOffer(id)
        if (!checkAuth(currentOffer, jwt).contains(Permission.UPDATE)) throw UnauthenticatedException()
        this.updateOffer(
            currentOffer,
            offerRequest.toOffer(
                currentOffer.creator,
                desiredSkillService
            )
        )
    }

    fun deleteOffer(id: Int, jwt: String?) {
        if (!checkAuth(this.getOffer(id), jwt).contains(Permission.DELETE)) throw UnauthenticatedException()
        deleteOffer(id)
    }

    private fun deleteOffer(id: Int) = offerRepository.deleteById(id)

    private fun checkAuth(offer: Offer, jwt: String?): Set<Permission> {
        val user = securityService.getHrPartnerFromJWT(jwt) ?: securityService.getOrganizationFromJWT(jwt)
        ?: throw UnauthenticatedException()
        val permissions = mutableSetOf<Permission>()
        if (user is HrPartner && offer.creator.id == user.id) {
            permissions.addAll(Permission.allPermissions())
        }
        if (user is Organization && offer.creator.organization.id == user.id) {
            permissions.addAll(setOf(Permission.UPDATE, Permission.DELETE))
        }
        if (securityService.isCorrectApiKey(jwt)) permissions.addAll(Permission.allPermissions())
        return permissions
    }
}
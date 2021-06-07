package demo.services

import demo.models.offers.DesiredSkill
import demo.models.offers.Offer
import demo.repositories.OfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OfferService(@Autowired private val offerRepository: OfferRepository) {

    fun getOffers(): MutableIterable<Offer> = offerRepository.findByOrderByIdDesc()

    fun getOffer(id: Int): Offer = offerRepository.findById(id).get()

    fun getOfferDesiredSkills(id: Int): MutableIterable<DesiredSkill> = getOffer(id).skills.toMutableSet()

    fun addOffer(offer: Offer): Offer = offerRepository.save(offer)

    fun updateOffer(id: Int, offer: Offer) {
        val currOffer: Optional<Offer> = offerRepository.findById(id)
        if (currOffer.isPresent) {
            val updated: Offer = currOffer.get().copy(name = offer.name,
                    description = offer.description,
                    position = offer.position,
                    minSalary = offer.minSalary,
                    maxSalary = offer.maxSalary,
                    localization = offer.localization,
                    skills = offer.skills
            )

            offerRepository.save(updated)
        }
    }

    fun deleteOffer(id: Int) = offerRepository.deleteById(id)

}
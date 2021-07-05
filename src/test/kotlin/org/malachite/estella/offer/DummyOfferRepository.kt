package org.malachite.estella.offer

import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.offer.domain.OfferRepository
import java.util.*

class DummyOfferRepository: OfferRepository {

    private val offers: MutableList<Offer> = mutableListOf()

    override fun findByOrderByIdDesc(): MutableList<Offer> =
        offers.sortedByDescending { it.id } as MutableList<Offer>

    override fun findById(offerId: Int): Optional<Offer> =
        Optional.ofNullable(offers.firstOrNull { it.id == offerId })


    override fun save(offer: Offer): Offer {
        this.deleteById(offer.id!!)
        offers.add(offer)
        return offer
    }

    override fun deleteById(id: Int) {
        offers.remove(offers.find { it.id == id })
    }

    fun clear() {
        this.offers.clear()
    }

    fun size() = this.offers.size
}
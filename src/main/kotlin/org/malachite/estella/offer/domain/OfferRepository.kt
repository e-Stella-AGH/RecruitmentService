package org.malachite.estella.offer.domain

import org.malachite.estella.commons.models.offers.Offer
import java.util.*

interface OfferRepository {
    fun findByOrderByIdDesc(): MutableList<Offer>
    fun findById(offerId: Int): Optional<Offer>
    fun save(offer: Offer): Offer
    fun deleteById(id: Int)
}
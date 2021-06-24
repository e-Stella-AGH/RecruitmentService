package org.malachite.estella.offer.infrastructure;

import org.malachite.estella.commons.models.offers.Offer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OfferRepository: CrudRepository<Offer, Int> {
    fun findByOrderByIdDesc(): MutableList<Offer>
}

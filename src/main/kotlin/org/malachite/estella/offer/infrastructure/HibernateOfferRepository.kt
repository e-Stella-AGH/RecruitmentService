package org.malachite.estella.offer.infrastructure;

import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.offer.domain.OfferRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateOfferRepository: CrudRepository<Offer, Int>, OfferRepository {
    override fun findByOrderByIdDesc(): MutableList<Offer>
}

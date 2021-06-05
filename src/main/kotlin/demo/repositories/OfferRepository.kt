package demo.repositories

import demo.models.offers.Offer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OfferRepository: CrudRepository<Offer, Int> {
}
package demo.repositories

import demo.models.people.HrPartner
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HrPartnerRepository: CrudRepository<HrPartner, Int> {
}
package org.malachite.estella.people.infrastrucutre;

import org.malachite.estella.commons.models.people.HrPartner
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HrPartnerRepository: CrudRepository<HrPartner, Int> {
}

package org.malachite.estella.people.infrastrucutre;

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.people.domain.HrPartnerRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateHrPartnerRepository: CrudRepository<HrPartner, Int>, HrPartnerRepository

package org.malachite.estella.process.infrastructure;

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RecruitmentProcessRepository: CrudRepository<RecruitmentProcess, Int> {
}

package org.malachite.estella.process.infrastructure;

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.process.domain.RecruitmentProcessRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateRecruitmentProcessRepository: CrudRepository<RecruitmentProcess, Int>, RecruitmentProcessRepository

package org.malachite.estella.process.infrastructure

import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.springframework.data.repository.CrudRepository

interface HibernateRecruitmentStageRepository: CrudRepository<RecruitmentStage, Int>, RecruitmentStageRepository {

}
package org.malachite.estella.aplication.infrastructure;

import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository: CrudRepository<Application, Int> {
    fun getAllByStageIn(stage: List<RecruitmentStage>): List<Application>
    fun getAllByJobSeekerId(jobSeekerId: Int): List<Application>
}

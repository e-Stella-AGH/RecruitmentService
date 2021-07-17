package org.malachite.estella.services

import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RecruitmentStageService(
    @Autowired private val repository: RecruitmentStageRepository
) {
    fun findOrCreate(stageType: StageType): RecruitmentStage {
        val optionalType = repository.findByType(stageType)
        if(optionalType.isPresent) return optionalType.get()
        return repository.save(RecruitmentStage(type = stageType, id = null))
    }
}
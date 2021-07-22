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

    fun save(stage: RecruitmentStage) = repository.save(stage)

    fun save(type: StageType) = repository.save(RecruitmentStage(null, type))

    fun delete(stage: RecruitmentStage) = stage.id?.let { repository.deleteById(it) }

    fun getAllStagesTypesInUsage() =
        repository.findAll()
            .distinctBy { it.type }
            .map { it.type }

    fun getAllStagesTypes() = StageType.values()

}
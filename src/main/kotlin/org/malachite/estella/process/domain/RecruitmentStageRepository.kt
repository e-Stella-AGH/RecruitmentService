package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import java.util.*

interface RecruitmentStageRepository {
    fun save(stage: RecruitmentStage): RecruitmentStage
    fun findByType(type: StageType): Optional<RecruitmentStage>
    fun findById(id: Int): Optional<RecruitmentStage>
    fun deleteById(id: Int)
}
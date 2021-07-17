package org.malachite.estella.process.recruitmentStage

import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.RecruitmentStageRepository
import java.util.*

class DummyRecruitmentStageRepository : RecruitmentStageRepository {
    private val stages: MutableList<RecruitmentStage> = mutableListOf()

    override fun save(stage: RecruitmentStage): RecruitmentStage {
        stage.copy(id = 1).let {
            this.stages.add(it)
            return it
        }
    }

    override fun findByType(type: StageType): Optional<RecruitmentStage> =
        this.stages.firstOrNull { it.type == type }
            .let { Optional.ofNullable(it) }


    fun clear() = this.stages.clear()
    fun size() = this.stages.size

    fun add(stage: RecruitmentStage) {
        this.stages.add(stage)
    }
}
package org.malachite.estella.process

import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.RecruitmentStageRepository
import java.util.*

class DummyStageRepository : RecruitmentStageRepository {

    private val stages: MutableList<RecruitmentStage> = mutableListOf()

    override fun save(stage: RecruitmentStage): RecruitmentStage {
        stage.id?.let {
            if (it < stages.size) {
                stages[it] = stage.copy(id = it)
            } else {
                stages.add(stage.copy(id = this.stages.size))
            }
        } ?: stages.add(stage.copy(id = this.stages.size))
        return stage
    }

    override fun findByType(type: StageType): Optional<RecruitmentStage> =
        stages.firstOrNull { it.type == type }.let {
            Optional.ofNullable(it)
        }

    override fun findById(id: Int): Optional<RecruitmentStage> =
        stages.firstOrNull { it.id == id }.let {
            Optional.ofNullable(it)
        }

    override fun deleteById(id: Int) {
        stages.removeIf { it.id == id }
    }

    fun getAll() = stages

    fun clear() = stages.clear()
    fun size() = stages.size
}
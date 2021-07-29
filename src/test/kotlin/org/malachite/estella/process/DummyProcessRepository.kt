package org.malachite.estella.process

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.process.domain.RecruitmentProcessRepository
import java.util.*

class DummyProcessRepository(
    private val stagesRepository: DummyStageRepository
) : RecruitmentProcessRepository {

    private val processes: MutableList<RecruitmentProcess> = mutableListOf()

    override fun save(process: RecruitmentProcess): RecruitmentProcess =
        process.copy(id = this.size()).let {
            processes.add(it)
            it
        }

    override fun findAll(): MutableIterable<RecruitmentProcess> = processes

    override fun findById(id: Int): Optional<RecruitmentProcess> =
        processes.firstOrNull { it.id == id }.let {
            Optional.ofNullable(it?.copy(stages = stagesRepository.getAll()))
        }

    override fun deleteById(id: Int) {
        processes.removeIf { it.id == id }
    }

    fun clear() = processes.clear()
    fun size() = processes.size
}
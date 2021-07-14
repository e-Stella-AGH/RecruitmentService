package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import java.util.*

interface RecruitmentProcessRepository {
    fun save(process: RecruitmentProcess): RecruitmentProcess
    fun findAll(): MutableIterable<RecruitmentProcess>
    fun findById(id: Int): Optional<RecruitmentProcess>
    fun deleteById(id: Int)
}
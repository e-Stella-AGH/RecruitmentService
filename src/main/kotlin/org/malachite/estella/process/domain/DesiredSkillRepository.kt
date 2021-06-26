package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.offers.DesiredSkill
import java.util.*

interface DesiredSkillRepository {
    fun save(skill: DesiredSkill): DesiredSkill
    fun findAll(): MutableIterable<DesiredSkill>
    fun findById(id: Int): Optional<DesiredSkill>
    fun delete(skill: DesiredSkill)
}
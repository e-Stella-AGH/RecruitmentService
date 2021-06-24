package org.malachite.estella.services

import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.process.infrastructure.DesiredSkillRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DesiredSkillService(@Autowired private val desiredSkillRepository: DesiredSkillRepository) {
    fun getDesiredSkills(): MutableIterable<DesiredSkill> = desiredSkillRepository.findAll();

    fun getDesiredSkill(id: Pair<String, SkillLevel>): DesiredSkill =
        getDesiredSkills().first { skillsEquals(id, it) }

    fun getDesiredSkill(id: Int): DesiredSkill = desiredSkillRepository.findById(id).get()

    fun safeGetDesiredSkill(id: Pair<String, SkillLevel>): DesiredSkill? = getDesiredSkills().firstOrNull { skillsEquals(id, it) }

    fun addDesiredSkill(skill: DesiredSkill): DesiredSkill = desiredSkillRepository.save(skill)

    fun addDesiredSkills(skills: Iterable<DesiredSkill>): MutableIterable<DesiredSkill> = desiredSkillRepository.saveAll(skills)

    fun updateDesiredSkill(id: Int, skill: DesiredSkill) {
        val currSkill: DesiredSkill = getDesiredSkill(id)
        val updated: DesiredSkill = currSkill.copy(name = skill.name, level = skill.level)

        desiredSkillRepository.save(updated)
    }

    fun deleteDesiredSkill(id: Pair<String, SkillLevel>) = desiredSkillRepository.delete(getDesiredSkill(id))

    private fun skillsEquals(id: Pair<String, SkillLevel>, skill: DesiredSkill) =
        (skill.name == id.first) and (skill.level == id.second)
}
package org.malachite.estella.process.infrastructure

import org.malachite.estella.commons.models.offers.DesiredSkill
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DesiredSkillRepository: CrudRepository<DesiredSkill, Int> {
}
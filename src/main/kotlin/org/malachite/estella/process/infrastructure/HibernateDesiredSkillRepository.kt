package org.malachite.estella.process.infrastructure

import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.process.domain.DesiredSkillRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateDesiredSkillRepository: CrudRepository<DesiredSkill, Int>, DesiredSkillRepository
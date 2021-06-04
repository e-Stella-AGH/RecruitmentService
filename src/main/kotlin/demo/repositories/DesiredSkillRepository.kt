package demo.repositories

import demo.models.offers.DesiredSkill
import demo.models.offers.SkillLevel
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DesiredSkillRepository: CrudRepository<DesiredSkill, Int> {
}
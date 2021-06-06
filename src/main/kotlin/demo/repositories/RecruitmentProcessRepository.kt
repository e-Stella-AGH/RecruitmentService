package demo.repositories;

import demo.models.offers.RecruitmentProcess
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RecruitmentProcessRepository: CrudRepository<RecruitmentProcess, Int> {
}

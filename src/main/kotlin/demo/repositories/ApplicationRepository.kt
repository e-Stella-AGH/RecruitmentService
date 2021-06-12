package demo.repositories;

import demo.models.offers.Application;
import demo.models.offers.RecruitmentStage
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationRepository: CrudRepository<Application, Int> {
    fun getAllByStageIn(stage: List<RecruitmentStage>): List<Application>
    fun getAllByJobSeekerId(jobSeekerId: Int): List<Application>
}

package demo.repositories

import demo.models.interviews.Interview
import demo.models.offers.Application
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewRepository: CrudRepository<Interview, Int> {
}
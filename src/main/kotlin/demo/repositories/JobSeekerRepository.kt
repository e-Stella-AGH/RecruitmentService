package demo.repositories

import demo.models.people.JobSeeker
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JobSeekerRepository: CrudRepository<JobSeeker, Int> {
    fun findByUserId(user_id: Int): Optional<JobSeeker>
}

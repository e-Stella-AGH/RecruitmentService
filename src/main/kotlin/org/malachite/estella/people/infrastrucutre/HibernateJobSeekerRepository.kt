package org.malachite.estella.people.infrastrucutre

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.people.domain.JobSeekerRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HibernateJobSeekerRepository: CrudRepository<JobSeeker, Int>, JobSeekerRepository {
    override fun findByUserId(userId: Int): Optional<JobSeeker>
}

package org.malachite.estella.interview.infracstructure

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.interview.domain.InterviewRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HibernateInterviewRepository: CrudRepository<Interview, UUID>, InterviewRepository {
    override fun getAllByApplicationId(applicationId: Int): List<Interview>
}
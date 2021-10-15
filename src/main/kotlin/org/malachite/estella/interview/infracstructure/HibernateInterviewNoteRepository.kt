package org.malachite.estella.interview.infracstructure

import org.malachite.estella.commons.models.interviews.InterviewNote
import org.malachite.estella.interview.domain.InterviewNoteRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateInterviewNoteRepository: CrudRepository<InterviewNote, Int>, InterviewNoteRepository {
}
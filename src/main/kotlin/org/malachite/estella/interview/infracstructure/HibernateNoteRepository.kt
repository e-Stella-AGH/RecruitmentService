package org.malachite.estella.interview.infracstructure

import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.interview.domain.NoteRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateNoteRepository: CrudRepository<Note, Int>, NoteRepository {
}
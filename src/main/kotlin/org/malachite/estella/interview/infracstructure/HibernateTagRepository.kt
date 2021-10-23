package org.malachite.estella.interview.infracstructure

import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.interviews.Tag
import org.malachite.estella.interview.domain.NoteRepository
import org.malachite.estella.interview.domain.TagRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateTagRepository: CrudRepository<Tag, Int>, TagRepository {
}
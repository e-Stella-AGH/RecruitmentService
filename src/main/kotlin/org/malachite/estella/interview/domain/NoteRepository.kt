package org.malachite.estella.interview.domain

import org.malachite.estella.commons.models.interviews.Note
import java.util.*

interface NoteRepository {
    fun findById(id: Int): Optional<Note>
    fun deleteById(id: Int)
    fun save(interviewNote: Note): Note
}
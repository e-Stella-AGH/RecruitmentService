package org.malachite.estella.interview.domain

import org.malachite.estella.commons.models.interviews.InterviewNote
import java.util.*

interface InterviewNoteRepository {
    fun findById(id: Int): Optional<InterviewNote>
    fun deleteById(id: Int)
    fun save(interviewNote: InterviewNote): InterviewNote
}
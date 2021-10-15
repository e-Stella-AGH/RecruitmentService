package org.malachite.estella.interview.domain

import org.malachite.estella.commons.models.interviews.Interview
import java.util.*

interface InterviewRepository {
    fun findAll(): List<Interview>
    fun findById(id: UUID): Optional<Interview>
    fun deleteById(id: UUID)
    fun save(interview: Interview): Interview
}
package org.malachite.estella.interview.domain

import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.commons.models.interviews.Interview
import java.sql.Timestamp
import java.time.Instant
import java.util.*

data class InterviewPayload(val dateTime: Timestamp? = Timestamp.from(Instant.now()),
                            val minutesLength: Int? = 30)

data class InterviewId(val interviewId: String) {
    fun toUUID(): UUID {
        try {
            return UUID.fromString(interviewId)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            throw InvalidUUIDException()
        }
    }
}

fun Interview.getId() = InterviewId(this.id.toString())

data class InterviewDTO(val id: String?, val dateTime: Timestamp?, val minutesLength: Int,
val application: ApplicationDTO, val hosts: List<String>?, val notes: Set<InterviewNoteDTO>?)
data class InterviewNoteDTO(val id: Int?, val note: String)

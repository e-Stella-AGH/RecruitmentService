package org.malachite.estella.interview.domain

import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.aplication.domain.toApplicationDTO
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.Note
import java.sql.Timestamp
import java.util.*

data class InterviewPayload(val dateTime: Timestamp? = null,
                            val minutesLength: Int? = null)

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
fun Interview.toInterviewDTO() = InterviewDTO(this.id.toString(), this.dateTime, this.minutesLength, this.applicationStage.application.toApplicationDTO(), this.hosts)
fun Note.toInterviewNoteDTO() = InterviewNoteDTO(this.id, String(Base64.getDecoder().decode(this.text.toString())))
data class InterviewDTO(val id: String?, val dateTime: Timestamp?, val minutesLength: Int?,
val application: ApplicationDTO, val hosts: List<String>?)
data class InterviewNoteDTO(val id: Int?, val note: String)

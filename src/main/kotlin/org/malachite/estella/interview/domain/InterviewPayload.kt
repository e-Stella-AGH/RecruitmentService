package org.malachite.estella.interview.domain

import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.aplication.domain.toApplicationDTO
import org.malachite.estella.commons.PayloadUUID
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.toBase64String
import java.sql.Timestamp

data class InterviewPayload(
    val dateTime: Timestamp? = null,
    val minutesLength: Int? = null
)



fun Interview.getId() = PayloadUUID(this.id.toString())
fun Interview.toInterviewDTO() = InterviewDTO(
    this.id.toString(),
    this.dateTime,
    this.minutesLength,
    this.applicationStage.application!!.toApplicationDTO(),
    this.applicationStage.hosts
)

fun Note.toInterviewNoteDTO() = InterviewNoteDTO(this.id, this.text.toBase64String())
data class InterviewDTO(
    val id: String?, val dateTime: Timestamp?, val minutesLength: Int?,
    val application: ApplicationDTO, val hosts: Set<String>?
)

data class InterviewNoteDTO(val id: Int?, val note: String)

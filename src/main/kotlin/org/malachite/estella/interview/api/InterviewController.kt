package org.malachite.estella.interview.api

import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.interviews.InterviewNote
import org.malachite.estella.interview.domain.InterviewId
import org.malachite.estella.services.InterviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialClob

@RestController
@RequestMapping("/api/interview")
class InterviewController(
        @Autowired private val interviewService: InterviewService
) {

    @CrossOrigin
    @GetMapping("/jobseeker/{interviewId}")
    fun getJobseekerName(@PathVariable interviewId: InterviewId): ResponseEntity<JobseekerName> =
        interviewService.getUserFromInterviewUuid(interviewId.toUUID())
                .let {
                    ResponseEntity.ok(JobseekerName(it?.firstName, it?.lastName))
                }


    @CrossOrigin
    @GetMapping("/newest/{applicationId}")
    fun getNewestInterview(@PathVariable applicationId: Int): ResponseEntity<Any> =
            interviewService.getLastInterviewFromApplicationId(applicationId).let { ResponseEntity.ok(it) }

    @CrossOrigin
    @PutMapping("/{meetingId}/set-hosts")
    fun setHosts(@PathVariable meetingId: InterviewId, @RequestBody hosts: MeetingHosts): ResponseEntity<Any> =
            interviewService.setHosts(meetingId.toUUID(), hosts.hostsMails)
                    .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{meetingId}/pick-date")
    fun pickDate(@PathVariable meetingId: InterviewId, @RequestBody date: MeetingDate): ResponseEntity<Any> =
            interviewService.setDate(meetingId.toUUID(), date.dateTime)
                    .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @Transactional
    @PutMapping("/{meetingId}/add-notes")
    fun addNotes(@PathVariable meetingId: InterviewId, @RequestBody notes: MeetingNotes): ResponseEntity<Any> =
            interviewService.setNotes(meetingId.toUUID(), notes.notes.map { it.toNotes() }.toSet())
                    .let { OwnResponses.SUCCESS }

    fun NotesFilePayload.toNotes() = InterviewNote(this.id, SerialClob(String(Base64.getDecoder().decode(this.fileBase64)).toCharArray()))

}


data class JobseekerName(val firstName: String?, val lastName: String?)
data class NotesFilePayload(val id: Int?, val fileBase64: String)
data class MeetingNotes(val notes: Set<NotesFilePayload>)
data class MeetingHosts(val hostsMails: List<String>)
data class MeetingDate(val dateTime: Timestamp)


package org.malachite.estella.interview.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.interview.domain.InterviewDTO
import org.malachite.estella.interview.domain.InterviewId
import org.malachite.estella.interview.domain.toInterviewDTO
import org.malachite.estella.services.ApplicationService
import org.malachite.estella.services.ApplicationStageDataService
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
    @Autowired private val interviewService: InterviewService,
    @Autowired private val applicationStageDataService: ApplicationStageDataService,
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
    fun getNewestInterviewId(@PathVariable applicationId: Int): ResponseEntity<InterviewId> =
        interviewService.getLastInterviewIdFromApplicationId(applicationId).let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/newest/{applicationId}/interview")
    fun getNewestInterview(@PathVariable applicationId: Int): ResponseEntity<InterviewDTO> =
        interviewService.getLastInterviewFromApplicationId(applicationId).let { ResponseEntity.ok(it.toInterviewDTO()) }

    @CrossOrigin
    @PutMapping("/{meetingId}/set-hosts")
    fun setHosts(@PathVariable meetingId: InterviewId, @RequestBody hosts: MeetingHosts): ResponseEntity<Any> =
        interviewService.setHosts(meetingId.toUUID(), hosts.hostsMails)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{meetingId}/set-length")
    fun setHosts(@PathVariable meetingId: InterviewId, @RequestBody length: MeetingLength): ResponseEntity<Any> =
        interviewService.setLength(meetingId.toUUID(), length.minutesLength)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{meetingId}/pick-date")
    fun pickDate(@PathVariable meetingId: InterviewId, @RequestBody date: MeetingDate): ResponseEntity<Any> =
        interviewService.setDate(meetingId.toUUID(), date.dateTime)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @Transactional
    @PutMapping("/{meetingId}/add-notes")
    fun addNotes(
        @PathVariable meetingId: InterviewId,
        @RequestHeader(EStellaHeaders.devPassword) password: String,
        @RequestBody notes: MeetingNotes
    ): ResponseEntity<Any> =
        applicationStageDataService.setNotesToInterview(
            meetingId.toUUID(),
            password,
            notes.notes
        ).let { OwnResponses.SUCCESS }

}


data class JobseekerName(val firstName: String?, val lastName: String?)
data class NotesFilePayload(val id: Int?, val fileBase64: String, val tags: Set<String>, val author:String)
data class MeetingNotes(val notes: Set<NotesFilePayload>)
data class MeetingHosts(val hostsMails: List<String>)
data class MeetingDate(val dateTime: Timestamp)
data class MeetingLength(val minutesLength: Int)


package org.malachite.estella.interview.api

import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.aplication.domain.toApplicationDTO
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.PayloadUUID
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.interview.domain.InterviewDTO
import org.malachite.estella.interview.domain.toInterviewDTO
import org.malachite.estella.services.ApplicationStageDataService
import org.malachite.estella.services.InterviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/api/interview")
class InterviewController(
    @Autowired private val interviewService: InterviewService,
    @Autowired private val applicationStageDataService: ApplicationStageDataService,
) {

    @CrossOrigin
    @GetMapping("/jobseeker/{interviewId}")
    fun getJobseekerName(@PathVariable interviewId: PayloadUUID): ResponseEntity<JobseekerName> =
        interviewService.getUserFromInterviewUuid(interviewId.toUUID())
            .let {
                ResponseEntity.ok(JobseekerName(it?.firstName, it?.lastName))
            }


    @CrossOrigin
    @GetMapping("/newest/{applicationId}")
    fun getNewestInterviewId(@PathVariable applicationId: Int): ResponseEntity<PayloadUUID> =
        interviewService.getLastInterviewIdFromApplicationId(applicationId).let { ResponseEntity.ok(it) }

    @CrossOrigin
    @Transactional
    @GetMapping("/newest/{applicationId}/interview")
    fun getNewestInterview(
        @PathVariable applicationId: Int,
        @RequestParam("with_possible_hosts") withPossibleHosts: Boolean = false
    ): ResponseEntity<InterviewWithPossibleHostsDTO> =
        interviewService.getLastInterviewFromApplicationId(applicationId, withPossibleHosts).let { ResponseEntity.ok(it.toInterviewWithPossibleHostsDTO()) }

    data class InterviewWithPossibleHostsDTO(
        val id: String?,
        val dateTime: Timestamp?,
        val minutesLength: Int?,
        val application: ApplicationDTO,
        val hosts: Set<String>,
        val possibleHosts: List<String>?
    )
    fun InterviewService.InterviewWithPossibleHosts.toInterviewWithPossibleHostsDTO() = InterviewWithPossibleHostsDTO(
        this.id.toString(),
        this.dateTime,
        this.minutesLength,
        this.applicationStage.application.toApplicationDTO(),
        this.applicationStage.hosts,
        this.possibleHosts
    )

    @CrossOrigin
    @PutMapping("/{meetingId}/set_hosts")
    fun setHosts(@PathVariable meetingId: PayloadUUID, @RequestBody hosts: MeetingHosts): ResponseEntity<Message> =
        interviewService.getInterviewWithCheckRights(meetingId.toUUID())
            .let { applicationStageDataService.setHostsForInterview(it.id!!, hosts.hostsMails.toMutableSet()) }
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{meetingId}/set_duration")
    fun setDuration(@PathVariable meetingId: PayloadUUID, @RequestBody length: MeetingLength): ResponseEntity<Message> =
        interviewService.setDuration(meetingId.toUUID(), length.minutesLength)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/{meetingId}/pick_date")
    fun pickDate(@PathVariable meetingId: PayloadUUID, @RequestBody date: MeetingDate): ResponseEntity<Message> =
        interviewService.setDate(meetingId.toUUID(), date.dateTime)
            .let { OwnResponses.SUCCESS }
}


data class JobseekerName(val firstName: String?, val lastName: String?)
data class NotesFilePayload(val id: Int?, val fileBase64: String, val tags: Set<String>, val author:String)
data class MeetingNotes(val notes: Set<NotesFilePayload>)
data class MeetingHosts(val hostsMails: Set<String>)
data class MeetingDate(val dateTime: Timestamp)
data class MeetingLength(val minutesLength: Int)


package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.InterviewNote
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.interview.domain.*
import org.malachite.estella.security.Authority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class InterviewService(
        @Autowired private val interviewRepository: InterviewRepository,
        @Autowired private val applicationRepository: ApplicationRepository,
        @Autowired private val hrPartnerService: HrPartnerService,
        @Autowired private val offerService: OfferService,
        @Autowired private val mailService: MailService,
        @Autowired private val recruitmentProcessService: RecruitmentProcessService,
        @Autowired private val interviewNoteRepository: InterviewNoteRepository,
        @Autowired private val securityService: SecurityService
): EStellaService<Interview>() {
    override val throwable: Exception = InterviewNotFoundException()

    fun createInterview(offer: Offer, application: Application, payload: InterviewPayload = InterviewPayload()) {
        val interview = Interview(null, payload.dateTime, payload.minutesLength, application, listOf(), setOf())
        interviewRepository.save(interview)
        mailService.sendInterviewInvitationMail(offer, interview)
    }

    fun createInterview(applicationId: Int, stage: StageType) {
        val minutesLength: Int = if (stage == StageType.HR_INTERVIEW) 30 else 90
        val application = applicationRepository.findById(applicationId)
        application.ifPresent {
            val interview = Interview(null, null, minutesLength, application.get(), listOf(), setOf())
            interviewRepository.save(interview)
            mailService.sendInterviewInvitationMail(offerService.getOfferByApplication(it), interview)
        }
    }


    fun getInterview(id: UUID): Interview = withExceptionThrower { interviewRepository.findById(id).get() }

    fun getUserFromInterviewUuid(interviewId: UUID): User? =
        getInterview(interviewId).application.jobSeeker.user

    fun getOrganizationFromPartner(hrPartnerId: Int): Organization =
            hrPartnerService.getHrPartner(hrPartnerId).organization

    fun getLastInterviewFromApplicationId(applicationId: Int): InterviewId =
            withExceptionThrower { interviewRepository.getAllByApplicationId(applicationId).sortedWith { a, b ->
                a.dateTime?.compareTo(b.dateTime) ?: -1
            }.first() }.getId()

    fun setHosts(id: UUID, hostsMails: List<String>) {
        if (!canHrUpdate(id)) throw UnauthenticatedException()
        val interview = getInterview(id)
        val savedInterview = interviewRepository.save(interview.copy(hosts=hostsMails))
        val application = savedInterview.application
        val offer = recruitmentProcessService.getProcessFromStage(application.stage).offer
        hostsMails.forEach { mail -> mailService.sendInterviewDevInvitationMail(offer, savedInterview, application, mail) }

    }

    fun setDate(id: UUID, dateTime: Timestamp) {
        if (!canJobSeekerUpdate(id)) throw UnauthenticatedException()
        val interview = getInterview(id)
        val savedInterview = interviewRepository.save(interview.copy(dateTime = dateTime))
        val application = interview.application
        val offer = recruitmentProcessService.getProcessFromStage(application.stage).offer
        mailService.sendInterviewDateConfirmationMail(offer, savedInterview, application, application.jobSeeker.user.mail)
        savedInterview.hosts.forEach { mail -> mailService.sendInterviewDateConfirmationMail(offer, savedInterview, application, mail) }

    }

    fun setNotes(id: UUID, notes: Set<InterviewNote> ) {
        if (!canHrUpdate(id)) throw UnauthenticatedException() //TODO dev security?
        val interview = getInterview(id)
        val savedNotes = mutableSetOf<InterviewNote>()
        notes.forEach { savedNotes.add(interviewNoteRepository.save(it)) }
        interviewRepository.save(interview.copy(notes = savedNotes))

    }

    fun canHrUpdate(id: UUID?): Boolean {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return true

        val user = userDetails?.user ?: throw UnauthenticatedException()
        val userAuthority = userDetails.authorities.firstOrNull() ?: throw UnauthenticatedException()
        val interview = interviewRepository.findById(id!!).get()

        if (userAuthority == Authority.hr && user.id ==
                recruitmentProcessService.getProcessFromStage(interview.application.stage).offer.creator.id)
            return true
        return false
    }

    fun canJobSeekerUpdate(id: UUID?): Boolean {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return true

        val user = userDetails?.user ?: throw UnauthenticatedException()
        val userAuthority = userDetails.authorities.firstOrNull() ?: throw UnauthenticatedException()
        val interview = interviewRepository.findById(id!!).get()

        if (userAuthority == Authority.job_seeker && user.id == interview.application.jobSeeker.id)
            return true
        return false
    }






}
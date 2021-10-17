package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.interviews.InterviewNote
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
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val mailService: MailService,
    @Autowired private val hrPartnerService: HrPartnerService,
    @Autowired private val offerService: OfferService,
    @Autowired private val interviewNoteRepository: InterviewNoteRepository,
    @Autowired private val securityService: SecurityService
): EStellaService<Interview>() {
    override val throwable: Exception = InterviewNotFoundException()

    fun createInterview(applicationStage: ApplicationStageData, payload: InterviewPayload = InterviewPayload()): Interview {
        val offer = recruitmentProcessService.getProcessFromStage(applicationStage).offer
        return Interview(null, payload.dateTime, payload.minutesLength, applicationStage, setOf(), setOf()).let {
            interviewRepository.save(it)
        }.also { mailService.sendInterviewInvitationMail(offer, it) }
    }

    fun getInterview(id: UUID): Interview = withExceptionThrower { interviewRepository.findById(id).get() }

    fun getUserFromInterviewUuid(interviewId: UUID): User? =
            getInterview(interviewId).applicationStage.application.jobSeeker.user

    fun getOrganizationFromPartner(hrPartnerId: Int): Organization =
            hrPartnerService.getHrPartner(hrPartnerId).organization

    fun getLastInterviewIdFromApplicationId(applicationId: Int): InterviewId =
            withExceptionThrower { getAllByApplicationId(applicationId).sortedWith { a, b ->
                a.dateTime?.compareTo(b.dateTime) ?: -1
            }.first() }.getId()

    fun getLastInterviewFromApplicationId(applicationId: Int): Interview =
            withExceptionThrower { getAllByApplicationId(applicationId).sortedWith { a, b ->
                a.dateTime?.compareTo(b.dateTime) ?: -1
            }.first() }

    fun setHosts(id: UUID, hostsMails: List<String>) {
        if (!canHrUpdate(id)) throw UnauthenticatedException()
        val interview = getInterview(id)
        val savedInterview = interviewRepository.save(interview.copy(hosts=hostsMails.toSet()))
        val application = savedInterview.applicationStage
        val offer = recruitmentProcessService.getProcessFromStage(application).offer
        hostsMails.forEach { mail -> mailService.sendInterviewDevInvitationMail(offer, savedInterview, application.application, mail) }
    }

    fun setLength(id: UUID, length: Int) {
        if (!canHrUpdate(id)) throw UnauthenticatedException()
        val interview = getInterview(id)
        interviewRepository.save(interview.copy(minutesLength= length))
    }

    fun setDate(id: UUID, dateTime: Timestamp) {
        val interview = getInterview(id)
        val savedInterview = interviewRepository.save(interview.copy(dateTime = dateTime))
        val applicationStage = interview.applicationStage
        val application = applicationStage.application
        val offer = recruitmentProcessService.getProcessFromStage(applicationStage).offer
        mailService.sendInterviewDateConfirmationMail(offer, savedInterview, application, application.jobSeeker.user.mail)
        savedInterview.hosts.forEach { mail -> mailService.sendInterviewDateConfirmationMail(offer, savedInterview, application, mail) }

    }

    fun setNotes(id: UUID, password: String, notes: Set<InterviewNote> ) {
        if (!canDevUpdate(id, password)) throw UnauthenticatedException()
        val savedNotes = mutableSetOf<InterviewNote>()
        notes.forEach { savedNotes.add(interviewNoteRepository.save(it)) }
        val interview = getInterview(id)
        interviewRepository.save(interview.copy(notes = savedNotes))

    }

    private fun canHrUpdate(id: UUID?): Boolean {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return true

        val user = userDetails?.user ?: throw UnauthenticatedException()
        val userAuthority = userDetails.authorities.firstOrNull() ?: throw UnauthenticatedException()
        val interview = interviewRepository.findById(id!!).get()

        if (userAuthority == Authority.hr && user.id ==
                recruitmentProcessService.getProcessFromStage(interview.applicationStage).offer.creator.id)
            return true
        return false
    }

    private fun canDevUpdate(id: UUID?, password: String): Boolean {
        id?: let { return false }
        id.let {
            val organization = recruitmentProcessService
                    .getProcessFromStage(getInterview(it).applicationStage)
                    .offer.creator.organization
            return securityService.compareOrganizationWithPassword(organization, password)
        }
    }

    private fun getAllByApplicationId(applicationId: Int): List<Interview> = interviewRepository
            .findAll()
            .filter { it.applicationStage.application.id == applicationId }







}

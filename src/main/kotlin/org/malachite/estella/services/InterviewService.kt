package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.PayloadUUID
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
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
    @Autowired private val securityService: SecurityService
) : EStellaService<Interview>() {
    override val throwable: Exception = InterviewNotFoundException()

    fun createInterview(
        applicationStage: ApplicationStageData,
        payload: InterviewPayload = InterviewPayload()
    ): Interview =
        Interview(null, payload.dateTime, payload.minutesLength, applicationStage)
            .let { interviewRepository.save(it) }

    fun getInterview(id: UUID): Interview = withExceptionThrower { interviewRepository.findById(id).get() }

    fun getUserFromInterviewUuid(interviewId: UUID): User? =
        getInterview(interviewId).applicationStage.application!!.jobSeeker.user

    fun getOrganizationFromPartner(hrPartnerId: Int): Organization =
        hrPartnerService.getHrPartner(hrPartnerId).organization

    fun getLastInterviewIdFromApplicationId(applicationId: Int): PayloadUUID =
        getLastInterviewFromApplicationId(applicationId).getId()

    fun getLastInterviewFromApplicationId(applicationId: Int): Interview =
        withExceptionThrower {
            getAllByApplicationId(applicationId).sortedBy { it.dateTime }.sortedWith { a, b ->
                when {
                    a.dateTime == null -> -1
                    b.dateTime == null -> 1
                    else -> a.dateTime.compareTo(b.dateTime)
                }

            }.first()
        }

    fun setDuration(id: UUID, length: Int) {
        if (!canHrUpdate(id)) throw UnauthenticatedException()
        val interview = getInterview(id)
        interviewRepository.save(interview.copy(minutesLength = length))
    }

    fun setDurationAndDate(id: UUID, length: Int, dateTime: Timestamp) {
        if (length <= 0) throw InvalidInterviewLengthException()
        else getInterview(id)
            .let { interviewRepository.save(it.copy(minutesLength = length)) }
            .also { setDate(id, dateTime) }
    }

    fun setDate(id: UUID, dateTime: Timestamp) {
        val interview = getInterview(id)
        val savedInterview = interviewRepository.save(interview.copy(dateTime = dateTime))
        val applicationStage = interview.applicationStage
        val application = applicationStage.application
        val offer = recruitmentProcessService.getProcessFromStage(applicationStage).offer
        mailService.sendInterviewJobSeekerConfirmationMail(
                offer,
                savedInterview,
                application,
                application.jobSeeker.user.mail
        )
    }

    fun getInterviewWithCheckRights(id: UUID): Interview =
        if (!canHrUpdate(id)) throw UnauthenticatedException()
        else getInterview(id)


    private fun canHrUpdate(id: UUID?): Boolean {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return true

        val user = userDetails?.user ?: throw UnauthenticatedException()
        val userAuthority = userDetails.authorities.firstOrNull() ?: throw UnauthenticatedException()
        val interview = interviewRepository.findById(id!!).get()

        if (userAuthority == Authority.hr && user.id ==
            recruitmentProcessService.getProcessFromStage(interview.applicationStage).offer.creator.id
        )
            return true
        return false
    }


    private fun getAllByApplicationId(applicationId: Int): List<Interview> = interviewRepository
        .findAll()
        .filter { it.applicationStage.application!!.id == applicationId }

    fun getTaskStageUUID(interviewId: UUID) =
        this.getInterview(interviewId).applicationStage.tasksStage?.id


}

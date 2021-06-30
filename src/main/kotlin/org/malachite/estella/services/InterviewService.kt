package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.process.domain.InterviewPayloads
import org.malachite.estella.process.domain.InterviewRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InterviewService(
    @Autowired private val interviewRepository: InterviewRepository,
    @Autowired private val mailService: MailService
) {

    fun createInterview(offer: Offer, application: Application, payload: InterviewPayloads = InterviewPayloads()) {
        val interview = Interview(null, payload.dateTime, payload.minutesLength, application, setOf())
        interviewRepository.save(interview)
        mailService.sendMail(mailService.getInterviewInvitationAsMailPayload(offer, interview))
    }

}
package demo.services

import demo.dto.interview.InterviewPayloads
import demo.models.interviews.Interview
import demo.models.offers.Application
import demo.models.offers.Offer
import demo.repositories.InterviewRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class InterviewService(@Autowired private val interviewRepository: InterviewRepository) {

    fun createInterview(offer: Offer, application: Application, payload: InterviewPayloads = InterviewPayloads()) {
        val interview = Interview(null, payload.dateTime, payload.minutesLength, application, setOf())
        interviewRepository.save(interview)
        MailService.sendMail(MailService.getInterviewInvitationAsMailPayload(offer, interview))
    }

}
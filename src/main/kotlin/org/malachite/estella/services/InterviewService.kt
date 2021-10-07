package org.malachite.estella.services

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.process.domain.InterviewPayloads
import org.malachite.estella.process.domain.InterviewRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InterviewService(
    @Autowired private val interviewRepository: InterviewRepository,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val mailService: MailService
) {

    fun createInterview(applicationStage: ApplicationStageData, payload: InterviewPayloads = InterviewPayloads()): Interview {
        val offer = recruitmentProcessService.getProcessFromStage(applicationStage).offer
        return Interview(null, payload.dateTime, payload.minutesLength, applicationStage).let {
            interviewRepository.save(it)
        }.also { mailService.sendInterviewInvitationMail(offer, it) }
    }

}
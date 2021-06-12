package demo.services

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.repositories.ApplicationRepository
import demo.repositories.JobSeekerRepository
import demo.repositories.OfferRepository
import demo.repositories.RecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.NoSuchElementException

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val jobSeekerRepository: JobSeekerRepository,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val interviewService: InterviewService
) {
    fun insertApplicationLoggedInUser(offerId: Int, applicationPayload: ApplicationLoggedInPayload) {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.findByUserId(applicationPayload.userId).get()
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        stage?.let {
            val application = applicationRepository.save(
                applicationPayload.toApplication(it, jobSeeker)
            )
            if (application.seekerFiles.isNotEmpty()) {
                val updatedSeekerFiles = jobSeeker.files.plus(application.seekerFiles)
                val updatedJobSeeker = jobSeeker.copy(files = updatedSeekerFiles)
                jobSeekerRepository.save(updatedJobSeeker)
            }
            MailService.sendMail(MailService.getApplicationConfirmationAsMailPayload(offer, application))
        } ?: throw NoSuchElementException()
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload) {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.save(applicationPayload.toJobSeeker())
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        stage?.let {
            val application = applicationPayload.toApplication(it, jobSeeker)
            val newApplication = applicationRepository.save(application)
            MailService.sendMail(MailService.getApplicationConfirmationAsMailPayload(offer, newApplication))
            setNextStageOfApplication(newApplication.id!!)
            interviewService.createInterview(offer, newApplication)
        } ?: throw NoSuchElementException()

    }

    fun setNextStageOfApplication(applicationId: Int) {
        val application = applicationRepository.findById(applicationId).get()
        val recruitmentProcessStages = recruitmentProcessService
            .getProcessFromStage(application.stage)
            .stages
            .sortedBy { it.id }
        val index = recruitmentProcessStages.indexOf(application.stage)
        if (index + 1 < recruitmentProcessStages.size)
            applicationRepository.save(application.copy(stage = recruitmentProcessStages[index + 1]))
    }


}
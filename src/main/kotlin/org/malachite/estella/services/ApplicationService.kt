package org.malachite.estella.services

import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.aplication.domain.ApplicationLoggedInPayload
import org.malachite.estella.aplication.domain.ApplicationNoUserPayload
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.offer.domain.OfferRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val jobSeekerRepository: JobSeekerRepository,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val interviewService: InterviewService
) {
    fun insertApplicationLoggedInUser(offerId: Int, applicationPayload: ApplicationLoggedInPayload): Application {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.findByUserId(applicationPayload.userId).get()
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        return stage?.let {
            val application = applicationRepository.save(
                applicationPayload.toApplication(it, jobSeeker)
            )
            if (application.seekerFiles.isNotEmpty()) {
                val updatedSeekerFiles = jobSeeker.files.plus(application.seekerFiles)
                val updatedJobSeeker = jobSeeker.copy(files = updatedSeekerFiles)
                jobSeekerRepository.save(updatedJobSeeker)
            }
            MailService.sendMail(MailService.getApplicationConfirmationAsMailPayload(offer, application))
            application
        } ?: throw NoSuchElementException()
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload): Application {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.save(applicationPayload.toJobSeeker())
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        return stage?.let {
            val application = applicationRepository.save(
                applicationPayload.toApplication(it, jobSeeker)
            )
            MailService.sendMail(MailService.getApplicationConfirmationAsMailPayload(offer, application))
            setNextStageOfApplication(application.id!!)
            interviewService.createInterview(offer, application)
            application
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

    fun getApplicationById(applicationId: Int): ApplicationDTO =
        ApplicationDTO.fromApplication(
            applicationRepository.findById(applicationId).get()
        )

    fun getAllApplications(): List<ApplicationDTO> =
        applicationRepository.findAll().map { ApplicationDTO.fromApplication(it) }

    fun getApplicationsByOffer(offerId: Int): List<ApplicationDTO> {
        val offer = offerRepository.findById(offerId).get()
        return offer.recruitmentProcess?.stages?.let { stage ->
            if (stage.isNotEmpty())
                applicationRepository.getAllByStageIn(stage).map { ApplicationDTO.fromApplication(it) }
            else
                Collections.emptyList()
        } ?: Collections.emptyList()
    }

    fun getApplicationsByJobSeeker(jobSeekerId: Int): List<ApplicationDTO> {
        return applicationRepository.getAllByJobSeekerId(jobSeekerId).map {
            ApplicationDTO.fromApplication(it)
        }
    }

    fun deleteApplication(applicationId: Int) =
        applicationRepository.deleteById(applicationId)
}
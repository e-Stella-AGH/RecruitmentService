package org.malachite.estella.services

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.people.JobSeeker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerService: OfferService,
    @Autowired private val jobSeekerService: JobSeekerService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val mailService: MailService
) : EStellaService<Application>() {

    override val throwable: Exception = ApplicationNotFoundException()

    fun insertApplicationLoggedInUser(
        offerId: Int,
        jobSeeker: JobSeeker,
        applicationPayload: ApplicationLoggedInPayload
    ): Application = insertApplication(offerId, jobSeeker, applicationPayload)

    fun insertApplication(offerId: Int, jobSeeker: JobSeeker, applicationPayload: ApplicationPayload): Application {
        val offer = offerService.getOffer(offerId)
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        return stage?.let {
            val files = jobSeekerService.addNewFiles(jobSeeker, applicationPayload.getJobSeekerFiles())
            val application = applicationRepository.save(applicationPayload.toApplication(it, jobSeeker, files))
            mailService.sendApplicationConfirmationMail(offer, application)
            application
        } ?: throw UnsupportedOperationException("First stage not found in application")
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload): Application =
        jobSeekerService.getOrCreateJobSeeker(applicationPayload.toJobSeeker())
            .let { insertApplication(offerId, it, applicationPayload) }


    fun setNextStageOfApplication(applicationId: Int) {
        val application = applicationRepository.findById(applicationId).get()

        if (application.status != ApplicationStatus.IN_PROGRESS)
            throw UnsupportedOperationException("Cannot change stage of resolved application!")

        val recruitmentProcessStages = recruitmentProcessService
            .getProcessFromStage(application.stage)
            .stages
            .sortedBy { it.id }
        val index = recruitmentProcessStages.indexOf(application.stage)
        if (index == recruitmentProcessStages.lastIndex - 1)
            applicationRepository.save(
                application.copy(
                    stage = recruitmentProcessStages[index + 1],
                    status = ApplicationStatus.ACCEPTED
                )
            )
        else if (index < recruitmentProcessStages.lastIndex)
            applicationRepository.save(application.copy(stage = recruitmentProcessStages[index + 1]))
    }

    fun getApplicationById(applicationId: Int): ApplicationDTO =
        withExceptionThrower { applicationRepository.findById(applicationId).get() }
            .toApplicationDTO()

    fun getAllApplications(): List<ApplicationDTO> =
        applicationRepository
            .findAll()
            .map { it.toApplicationDTO() }

    fun getApplicationsByOffer(offerId: Int): List<ApplicationDTOWithStagesListAndOfferName> =
        offerService.getOffer(offerId)
            .let {
                val stages = it.recruitmentProcess?.stages
                if (stages?.isNotEmpty() == true)
                    applicationRepository.getAllByStageIn(stages).map { application ->
                        application.toApplicationDTOWithStagesListAndOfferName(stages, it.name)
                    }
                else
                    Collections.emptyList()
            } ?: Collections.emptyList()

    fun getApplicationsByJobSeeker(jobSeekerId: Int): List<ApplicationDTO> =
        applicationRepository
            .getAllByJobSeekerId(jobSeekerId)
            .map { it.toApplicationDTO() }

    fun deleteApplication(applicationId: Int) =
        applicationRepository.deleteById(applicationId)

    fun rejectApplication(applicationId: Int) {
        applicationRepository.findById(applicationId).let {
            applicationRepository.save(it.get().copy(status = ApplicationStatus.REJECTED))
        }
    }

}
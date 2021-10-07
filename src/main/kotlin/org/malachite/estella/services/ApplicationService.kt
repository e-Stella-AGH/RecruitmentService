package org.malachite.estella.services

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.JobSeeker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerService: OfferService,
    @Autowired private val jobSeekerService: JobSeekerService,
    @Autowired private val applicationStageDataService: ApplicationStageDataService,
    @Autowired private val mailService: MailService
) : EStellaService<Application>() {

    override val throwable: Exception = ApplicationNotFoundException()

    fun insertApplicationLoggedInUser(
        offerId: Int,
        jobSeeker: JobSeeker,
        applicationPayload: ApplicationLoggedInPayload
    ): ApplicationDTO = insertApplication(offerId, jobSeeker, applicationPayload)

    fun insertApplication(offerId: Int, jobSeeker: JobSeeker, applicationPayload: ApplicationPayload): ApplicationDTO {
        val offer = offerService.getOffer(offerId)
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        return stage?.let {
            val files = jobSeekerService.addNewFiles(jobSeeker, applicationPayload.getJobSeekerFiles())
            val application = applicationRepository.save(applicationPayload.toApplication(it, jobSeeker, files))
            mailService.sendApplicationConfirmationMail(offer, application)
            addNewApplicationStage(application, it).toApplicationDTO()
        } ?: throw UnsupportedOperationException("First stage not found in application")
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload): ApplicationDTO =
        jobSeekerService.getOrCreateJobSeeker(applicationPayload.toJobSeeker())
            .let { insertApplication(offerId, it, applicationPayload) }


    fun setNextStageOfApplication(applicationId: Int, recruitmentProcess: RecruitmentProcess) {
        val application = applicationRepository.findById(applicationId).get()

        if (application.status != ApplicationStatus.IN_PROGRESS)
            throw UnsupportedOperationException("Cannot change stage of resolved application!")

        val recruitmentProcessStages = recruitmentProcess
            .stages
            .sortedBy { it.id }

        val applicationStages = application.applicationStages.map { it.stage }.sortedBy { it.id }

        if (applicationStages.isEmpty()) {
            addNewApplicationStage(application, recruitmentProcess.stages.first())
            return
        }

        val index = recruitmentProcessStages.indexOf(applicationStages.last())

        if (index < recruitmentProcessStages.lastIndex)
            addNewApplicationStage(application, recruitmentProcessStages[index + 1])
                .let {
                    if (index == recruitmentProcessStages.lastIndex - 1)
                        it.copy(status = ApplicationStatus.ACCEPTED)
                    else
                        it
                }.let { applicationRepository.save(it) }

    }

    private fun addNewApplicationStage(application: Application, recruitmentStage: RecruitmentStage) =
        applicationStageDataService.createApplicationStageData(
            application,
            recruitmentStage
        ).let {
            val stages = ArrayList(application.applicationStages.plus(it))
            val newApplication = application.copy(applicationStages = stages)
            newApplication
        }.let { applicationRepository.save(it) }


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
                    applicationRepository.findAll().toList().filter {
                        it.applicationStages
                            .map { it.stage }
                            .let { stages.intersect(it).isNotEmpty() }
                    }.map { application ->
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
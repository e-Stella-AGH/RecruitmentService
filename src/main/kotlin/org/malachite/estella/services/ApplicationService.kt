package org.malachite.estella.services

import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.process.domain.ProcessNotStartedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerService: OfferService,
    @Autowired private val jobSeekerService: JobSeekerService,
    @Autowired private val applicationStageDataService: ApplicationStageDataService,
    @Autowired private val recruitmentProcessService: RecruitmentProcessService,
    @Autowired private val taskStageService: TaskStageService,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val securityService: SecurityService,
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
        if (offer.recruitmentProcess == null || !offer.recruitmentProcess.isStarted()) throw ProcessNotStartedException()
        val stage = offer.recruitmentProcess.stages.getOrNull(0)
        return stage?.let {
            val files = jobSeekerService.addNewFiles(jobSeeker, applicationPayload.getJobSeekerFiles())
            val application = applicationRepository.save(applicationPayload.toApplication(it, jobSeeker, files))
            mailService.sendApplicationConfirmationMail(offer, application)
            application.addNewApplicationStageData(it, null)
        } ?: throw UnsupportedOperationException("First stage not found in application")
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload): Application =
            jobSeekerService.getOrCreateJobSeeker(applicationPayload.toJobSeeker())
                    .let { insertApplication(offerId, it, applicationPayload) }

    fun setNextStageOfApplication(applicationId: Int, recruitmentProcess: RecruitmentProcess, devs: MutableList<String>?) {
        val application = applicationRepository.findById(applicationId).get()

        if (application.status != ApplicationStatus.IN_PROGRESS)
            throw UnsupportedOperationException("Cannot change stage of resolved application!")

        val recruitmentProcessStages = recruitmentProcess
                .stages
                .sortedBy { it.id }

        val applicationRecruitmentStages = application.applicationStages.map { it.stage }.sortedBy { it.id }

        if (applicationRecruitmentStages.isEmpty()) {
            application.addNewApplicationStageData(recruitmentProcess.stages.first(), devs)
            return
        }

        val indexOfRecruitmentStage = recruitmentProcessStages.indexOf(applicationRecruitmentStages.last())

        if (isNotLastStage(indexOfRecruitmentStage, recruitmentProcessStages.lastIndex))
            application.addNewApplicationStageData(recruitmentProcessStages[indexOfRecruitmentStage + 1], devs)
                    .let {
                        if (shouldBeAccepted(indexOfRecruitmentStage, recruitmentProcessStages.lastIndex))
                            it.copy(status = ApplicationStatus.ACCEPTED)
                        else
                            it
                    }.let {
                        applicationRepository.save(it)
                    }
    }

    private fun isNotLastStage(currentIndex: Int, lastIndex: Int) = currentIndex < lastIndex

    private fun shouldBeAccepted(currentIndex: Int, lastIndex: Int) = currentIndex == lastIndex - 1

    private fun Application.addNewApplicationStageData(recruitmentStage: RecruitmentStage, devs: MutableList<String>?) =
            applicationStageDataService.createApplicationStageData(
                    this,
                    recruitmentStage,
                    devs
            ).let {
                val stages = ArrayList(this.applicationStages.plus(it))
                val newApplication = this.copy(applicationStages = stages)
                newApplication
            }.let { applicationRepository.save(it) }


    fun getApplicationById(applicationId: Int): Application =
            withExceptionThrower { applicationRepository.findById(applicationId).get() }

    fun getAllApplications(): List<Application> =
            applicationRepository
                    .findAll()


    fun getApplicationsWithStagesAndOfferName(offerId: Int): List<ApplicationWithStagesAndOfferName> =
        offerService.getOffer(offerId)
            .let {
                val stages = it.recruitmentProcess?.stages
                if (stages?.isNotEmpty() == true)
                    applicationRepository.findAll().toList().filter {
                        it.applicationStages
                            .map { it.stage }
                            .let { stages.intersect(it).isNotEmpty() }
                    }.map { application ->
                        ApplicationWithStagesAndOfferName(application, stages, it.name)
                    }
                else
                    Collections.emptyList()
            } ?: Collections.emptyList()

    fun getApplicationsByJobSeeker(jobSeekerId: Int): List<ApplicationWithStagesAndOfferName> =
        applicationRepository
            .getAllByJobSeekerId(jobSeekerId)
            .map { application ->
                Pair(application, recruitmentProcessService.getProcessFromStage(application.applicationStages[0]))
            }.map { pairs ->
                ApplicationWithStagesAndOfferName(
                    pairs.first,
                    pairs.second.stages,
                    pairs.second.offer.name
                )
            }

    fun deleteApplication(applicationId: Int) =
        applicationRepository.deleteById(applicationId)

    fun rejectApplication(applicationId: Int) {
        applicationRepository.findById(applicationId).let {
            applicationRepository.save(it.get().copy(status = ApplicationStatus.REJECTED))
        }
    }

    fun getApplicationsForDev(devMail: String, password: String): List<ApplicationForDevDTO> {
        return securityService.getTaskStageFromPassword(password).let { recruitmentProcessService.getProcessFromStage(it!!.applicationStage) }
                .let { process ->
                    val offer = process.offer
                    taskStageService.getByOrganization(offer.creator.organization)
                            .map { stage ->
                                ApplicationForDevDTO(
                                    stage.applicationStage.application.toApplicationDTO(),
                                    stage.id.toString(),
                                    stage.applicationStage.notes.map { it.toApplicationNoteDTO() }.toSet(),
                                    offer.position)
                            }
                }
    }


    data class ApplicationWithStagesAndOfferName(
        val application: Application,
        val stages: List<RecruitmentStage>,
        val offerName: String
    )

}
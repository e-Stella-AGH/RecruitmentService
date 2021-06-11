package demo.services

import demo.dto.application.ApplicationDTO
import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.models.offers.Application
import demo.repositories.ApplicationRepository
import demo.repositories.JobSeekerRepository
import demo.repositories.OfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val jobSeekerRepository: JobSeekerRepository
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
            MailService.sendMail(MailService.getMailPayloadFromApplication(offer, application))
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
            MailService.sendMail(MailService.getMailPayloadFromApplication(offer, application))
            application
        } ?: throw NoSuchElementException()
    }

    fun getApplicationById(applicationId: Int): ApplicationDTO =
        ApplicationDTO.fromApplication(
            applicationRepository.findById(applicationId).get()
        )

    fun getAllApplications(): List<ApplicationDTO> =
        applicationRepository.findAll().map { ApplicationDTO.fromApplication(it) }

    fun getApplicationsByOffer(offerId: Int): List<ApplicationDTO> {
        val offer = offerRepository.findById(offerId).get()
        return offer.recruitmentProcess?.stages?.let{ stage ->
            if (stage.isNotEmpty())
                applicationRepository.getAllByStageIn(stage).map{ ApplicationDTO.fromApplication(it) }
            else
                Collections.emptyList()
        } ?: Collections.emptyList()
    }

    fun deleteApplication(applicationId: Int) =
        applicationRepository.deleteById(applicationId)
}
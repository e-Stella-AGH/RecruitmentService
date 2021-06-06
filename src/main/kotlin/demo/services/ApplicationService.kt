package demo.services

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.models.offers.Application
import demo.models.offers.Offer
import demo.repositories.ApplicationRepository
import demo.repositories.JobSeekerRepository
import demo.repositories.OfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.http.HttpClient
import java.net.http.HttpRequest
import kotlin.NoSuchElementException

@Service
class ApplicationService(
    @Autowired private val applicationRepository: ApplicationRepository,
    @Autowired private val offerRepository: OfferRepository,
    @Autowired private val jobSeekerRepository: JobSeekerRepository
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
            MailService.send_mail(MailService.getMailPayloadFromApplication(offer, application))
        } ?: throw NoSuchElementException()
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload) {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.save(applicationPayload.toJobSeeker())
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        stage?.let {
            val application = applicationPayload.toApplication(it, jobSeeker)
            applicationRepository.save(application)
            MailService.send_mail(MailService.getMailPayloadFromApplication(offer, application))
        } ?: throw NoSuchElementException()
    }


}
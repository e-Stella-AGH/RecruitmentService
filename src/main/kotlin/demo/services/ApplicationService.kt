package demo.services

import demo.dto.application.ApplicationLoggedInPayload
import demo.dto.application.ApplicationNoUserPayload
import demo.repositories.ApplicationRepository
import demo.repositories.JobSeekerRepository
import demo.repositories.OfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
        stage?.let{
            applicationRepository.save(
                    applicationPayload.toApplication(it, jobSeeker)
            )} ?: throw NoSuchElementException()
    }

    fun insertApplicationWithoutUser(offerId: Int, applicationPayload: ApplicationNoUserPayload) {
        val offer = offerRepository.findById(offerId).get()
        val jobSeeker = jobSeekerRepository.save(applicationPayload.toJobSeeker())
        val stage = offer.recruitmentProcess?.stages?.getOrNull(0)
        stage?.let{
            applicationRepository.save(
                    applicationPayload.toApplication(it, jobSeeker)
            )} ?: throw NoSuchElementException()
    }
}
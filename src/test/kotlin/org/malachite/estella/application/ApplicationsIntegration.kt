package org.malachite.estella.application

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.UnauthenticatedMessage
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.people.domain.JobSeekerFilePayload
import org.malachite.estella.people.domain.toJobSeekerDTO
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.people.infrastrucutre.HibernateUserRepository
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.TestDatabaseReseter
import org.malachite.estella.util.hrPartners
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestExecutionListeners
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isNotNull

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ApplicationsIntegration : BaseIntegration() {

    @Autowired
    private lateinit var jobSeekerRepository: HibernateJobSeekerRepository

    @Autowired
    private lateinit var offerRepository: HibernateOfferRepository

    @Autowired
    private lateinit var userRepository: HibernateUserRepository

    @BeforeEach
    fun pri() {
        println("users: ${userRepository.findAll()}")
    }

    @Test
    @Order(1)
    fun `should be able to apply for offer as logged in user`() {
        EmailServiceStub.stubForSendEmail()

        val offer = getOffer()
        val jobSeeker = getJobSeeker()


        val response = httpRequest(
            path = "/api/applications/apply/${offer.id}/user",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobSeeker.user.mail, password)),
            body = mapOf("files" to setOf<JobSeekerFilePayload>())
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val responseBody = (response.body as Map<String, Any>).toApplicationDTO()
        val applications = getApplications()
        applications.find { it.id == responseBody.id }.let {
            expectThat(it).isNotNull()
            it!!
            expect {
                that(it.applicationDate).isEqualTo(responseBody.applicationDate)
                that(it.seekerFiles).isEqualTo(emptySet())
                that(it.stage).isEqualTo(responseBody.stage)
                that(it.status).isEqualTo(responseBody.status)
                that(it.status).isEqualTo(responseBody.status)
                that(it.jobSeeker).isEqualTo(jobSeeker.toJobSeekerDTO())
            }
        }
    }

    @Test
    @Order(2)
    fun `should not be able to apply for offer with bad jwt`() {

        val offer = getOffer()

        val response = httpRequest(
            path = "/api/applications/apply/${offer.id}/user",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.jwtToken to "12341412abcdce"),
            body = mapOf("files" to setOf<JobSeekerFilePayload>())
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        expectThat(response.body as Map<String,Any>).isEqualTo(mapOf("message" to UnauthenticatedMessage.message))
    }

    @Test
    @Order(3)
    fun `should be able to apply for offer as not logged in user`() {

        val offer = getOffer()

        val response = httpRequest(
            path = "/api/applications/apply/${offer.id}/no-user",
            method = HttpMethod.POST,
            body = mapOf(
                "firstName" to "Tolek",
                "lastName" to "Bolek",
                "mail" to applicationMail,
                "files" to setOf<JobSeekerFilePayload>()
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val responseBody = (response.body as Map<String, Any>).toApplicationDTO()
        val applications = getApplications()
        applications.find { it.id == responseBody.id }.let {
            expectThat(it).isNotNull()
            it!!
            expect {
                that(it.applicationDate).isEqualTo(responseBody.applicationDate)
                that(it.seekerFiles).isEqualTo(emptySet())
                that(it.stage).isEqualTo(responseBody.stage)
                that(it.status).isEqualTo(responseBody.status)
                that(it.status).isEqualTo(responseBody.status)
                that(it.jobSeeker.user.mail).isEqualTo(applicationMail)
            }
        }
    }

    @Test
    @Order(4)
    fun `should list all applications by offer`() {
        val offer = getOffer()
        val response = httpRequest(
            path = "/api/applications/offer/${offer.id}",
            method = HttpMethod.GET,
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val applications = (response.body as List<Map<String, Any>>).map { it.toApplicationDTO() }
        expectThat(applications.size).isGreaterThanOrEqualTo(2)
    }

    @Test
    @Order(5)
    fun `should list all applications by jobSeeker`() {

        val offer = getOffer()
        val jobSeeker = getJobSeeker()

        val response = httpRequest(
            path = "/api/applications/offer/${offer.id}",
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobSeeker.user.mail, password)),
            method = HttpMethod.GET,
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val applications = (response.body as List<Map<String, Any>>).map { it.toApplicationDTO() }
        expectThat(applications.size).isGreaterThanOrEqualTo(1)
    }

    @Test
    @Order(6)
    fun `should be able to change stage to next`() {
        val offer = getOffer()
        val application = getOfferApplications(offer!!).find { it.jobSeeker.user.mail == getJobSeeker().user.mail }!!
        expectThat(application.stage.type).isEqualTo(StageType.APPLIED)
        val response = updateStage(application)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedApplication = getApplication(application.id);
        expectThat(updatedApplication.stage.type).isEqualTo(StageType.HR_INTERVIEW)
    }

    @Test
    @Order(7)
    fun `should be able to change status to ended`() {
        val offer = getOffer()
        val application = getOfferApplications(offer!!).find { it.jobSeeker.user.mail == getJobSeeker().user.mail }!!
        expectThat(application.stage.type).isEqualTo(StageType.HR_INTERVIEW)
        expectThat(application.status).isEqualTo(ApplicationStatus.IN_PROGRESS)
        val response = updateStage(application)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedApplication = getApplication(application.id);
        println(updatedApplication)
        expectThat(updatedApplication.stage.type).isEqualTo(StageType.TECHNICAL_INTERVIEW)
        expectThat(updatedApplication.status).isEqualTo(ApplicationStatus.ACCEPTED)
    }

    @Test
    @Order(8)
    fun `should be able to change status to rejected`() {
        val offer = getOffer()
        val application = getOfferApplications(offer!!).find { it.jobSeeker.user.mail == applicationMail }!!
        expectThat(application.status).isEqualTo(ApplicationStatus.IN_PROGRESS)
        val response = httpRequest(
            path = "/api/applications/${application.id}/reject",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedApplication = getApplication(application.id);
        expectThat(updatedApplication.status).isEqualTo(ApplicationStatus.REJECTED)
    }

    private fun getApplications() =
        httpRequest(
            path = "/api/applications/",
            method = HttpMethod.GET
        ).also {
            expectThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }.body.let {
            it as List<Map<String, Any>>
            it.map { it.toApplicationDTO() }
        }

    private fun getOfferApplications(offer: Offer): List<ApplicationDTO> {
        val response = httpRequest(
            path = "/api/applications/offer/${offer.id}",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let {
            it as List<Map<String, Any>>
        }.map { it.toApplicationDTO() }
    }

    private fun getApplication(id: Int?): ApplicationDTO {
        val response = httpRequest(
            path = "/api/applications/${id}",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let {
            it as Map<String, Any>
        }.let { it.toApplicationDTO() }

    }

    private fun updateStage(application: ApplicationDTO) = httpRequest(
        path = "/api/applications/${application.id}/next",
        method = HttpMethod.PUT,
        headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
    )

    private fun loginUser(userMail: String, userPassword: String = password): BaseIntegration.Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getAuthToken(mail: String, password: String): String =
        loginUser(mail, password).headers!![EStellaHeaders.authToken]!![0]

    private fun getJobSeeker() = jobSeekerRepository.findAll().first()
    private fun getOffer() = offerRepository.findAll().first { it.creator.user.mail == hrPartner.user.mail}

    private val applicationMail = "examplemail@application.pl"
    private val password = "a"
    private val hrPartner = hrPartners[1]


}
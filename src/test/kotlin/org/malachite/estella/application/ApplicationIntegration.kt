package org.malachite.estella.application

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.loader.FakeDesiredSkills
import org.malachite.estella.commons.loader.FakeOffers
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import org.malachite.estella.people.domain.JobSeekerFilePayload
import org.malachite.estella.util.hrPartners
import org.malachite.estella.util.jobSeekers
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ApplicationIntegration: BaseIntegration() {

    @Test
    @Order(1)
    fun `should be able to apply for an offer`() {
        val offer = getOffer(partnersOffer)
        val response = httpRequest(
                path = "/api/applications/apply/${offer!!.id}/no-user",
                method = HttpMethod.POST,
                body = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "mail" to mail,
                    "files" to setOf<JobSeekerFilePayload>()
                )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val applications = getApplications(offer);
        expectThat(applications.find { it.jobSeeker.user.mail == mail }).isNotNull()
    }

    @Test
    @Order(2)
    fun `should be able to apply for an offer as user`() {
         getOffer(partnersOffer).let { offer ->
             println(offer)
             val response = httpRequest(
                 path = "/api/applications/apply/${offer!!.id}/user",
                 method = HttpMethod.POST,
                 headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
                 body = mapOf(
                     "files" to setOf<JobSeekerFilePayload>()
                 )
             )
             expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
             val applications = getApplications(offer);
             expectThat(applications.find { it.jobSeeker.user.mail == seeker.user.mail }).isNotNull()
        }

    }

    @Test
    @Order(3)
    fun `should be able to change stage to next`() {
        val offer = getOffer(partnersOffer)
        val application = getApplications(offer!!)[0]
        expectThat(application.stage.type).isEqualTo(StageType.APPLIED)
        val response = httpRequest(
            path = "/api/applications/${application.id}/next",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
            body = mapOf(
                "files" to setOf<JobSeekerFilePayload>()
            )
        )
        println(response.headers)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedApplication = getApplication(application.id);
        expectThat(updatedApplication.stage.type).isEqualTo(StageType.HR_INTERVIEW)
    }

    @Test
    @Order(4)
    fun `should be able to change status to ended`() {
        val offer = getOffer(partnersOffer)
        val application = getApplications(offer!!)[0]
        expectThat(application.stage.type).isEqualTo(StageType.HR_INTERVIEW)
        expectThat(application.status).isEqualTo(ApplicationStatus.IN_PROGRESS)
        val response = httpRequest(
            path = "/api/applications/${application.id}/next",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedApplication = getApplication(application.id);
        expectThat(updatedApplication.stage.type).isEqualTo(StageType.TECHNICAL_INTERVIEW)
        expectThat(updatedApplication.status).isEqualTo(ApplicationStatus.ACCEPTED)

    }

    @Test
    @Order(5)
    fun `should be able to change status to rejected`() {
        val offer = getOffer(partnersOffer)
        val application = getApplications(offer!!)[0]
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


    private fun getOffers() = httpRequest(
        path = "/api/offers",
        method = HttpMethod.GET
    ).also {
        expectThat(it.statusCode).isEqualTo(HttpStatus.OK)
    }.body.let {
        it as List<Map<String, Any>>
        it.map { it.toOfferResponse() }
    }

    private fun getOffer(offer: OfferResponse): OfferResponse? = getOffers().find { it.name == offer.name }

    private fun getApplications(offer: OfferResponse): List<ApplicationDTO> {
        println(offer)
        val response = httpRequest(
            path = "/api/applications/offer/${offer.id}",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let {
            it as List<Map<String, Any>>
        }.map { it.toApplicationDTO() }
    }

    private fun loginUser(userMail: String = seeker.user.mail, userPassword: String = password): Response {
        println(seeker.user)
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getAuthToken(mail:String = seeker.user.mail, userPassword: String = password):String =
        loginUser(mail, userPassword).headers?.get(EStellaHeaders.authToken)?.get(0)?:""


    private val firstName = "firstName"
    private val lastName = "lastName"
    private val mail = "examplemail@organization.pl"

    private val hrPartner = hrPartners[0]
    private val partnersOffer = FakeOffers.getOffers(listOf(hrPartner), FakeDesiredSkills.desiredSkills)[0].toOfferResponse()

    val password = "a"

    val seeker = jobSeekers[0]

}
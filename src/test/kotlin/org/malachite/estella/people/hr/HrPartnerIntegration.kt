package org.malachite.estella.people.hr

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.services.OfferService
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.hrPartners
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HrPartnerIntegration : BaseIntegration() {

    @Autowired
    private lateinit var hrPartnerRepository: HrPartnerRepository

    @Test
    @Order(1)
    fun `should add hrPartner to database`() {
        EmailServiceStub.stubForSendEmail()
        addOrganizationAndVerify()
        val response = addHrpartner(organizationMail)
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val users = getUsers()
        expectThat(users.find { it.mail == organizationMail }).isNotNull()
    }

    @Test
    @Order(2)
    fun `should not add hrpartner, because not login as organization`() {
        val response = addHrpartner(hrpartnerMail)
        withStatusAndMessage(response, "Unauthenticated", HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(3)
    fun `should return Bad Request with corresponding message, when user already exists`() {
        val response = addHrpartner(organizationMail)
        withStatusAndMessage(response, "Address already in use!", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(4)
    fun `should return ok and list of partner offers`(@Autowired offerService: OfferService) {
        val response = getPartnersOffers(legitHrPartner.user.mail, legitHrPartnerPassword)
        response.forEach{
            val offer = it.id?.let { it1 -> offerService.getOffer(it1) }
            offer?.let {
                expectThat(it.creator.user.mail).isEqualTo(legitHrPartner.user.mail)
            }
        }
    }

    @Test
    @Order(5)
    fun `should delete hrPartner`() {
        val partner = getHrPartners().first { it.user.mail == hrpartnerMail }
        hrPartnerRepository.save(partner.copy(user = partner.user.also { it.password = "a" }))
        val response = httpRequest(
            path = "/api/hrpartners/${partner.id}",
            method = HttpMethod.DELETE,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrpartnerMail, "a"))
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val deletedPartner = getHrPartners().firstOrNull { it.user.mail == hrpartnerMail }
        expectThat(deletedPartner).isEqualTo(null)
    }

    @Test
    @Order(6)
    fun `should delete hrPartner by mail`() {
        verifyOrganization(organizationMail)
        addHrpartner(organizationMail)

        val partner = getHrPartners().first { it.user.mail == hrpartnerMail }
        hrPartnerRepository.save(partner.copy(user = partner.user.also { it.password = "a" }))
        val response = httpRequest(
            path = "/api/hrpartners/mail",
            method = HttpMethod.DELETE,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(organizationMail, password)),
            body = mapOf("mail" to hrpartnerMail)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val deletedPartner = getHrPartners().firstOrNull { it.user.mail == hrpartnerMail }
        expectThat(deletedPartner).isNull()
    }

    private fun verifyOrganization(organizationMail: String) {
        val organization = getOrganization(organizationMail)

        val response = httpRequest(
            path = "/_admin/verify/${organization.id}",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.adminApiKey to API_KEY)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

    }

    private fun getOrganization(organizationMail: String): Organization {
        val response = httpRequest(
            path = "/api/organizations",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.adminApiKey to API_KEY)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let { body ->
            body as List<Map<String, Any>>
            body.map {
                it.toOrganization()
            }.first { it.user.mail == organizationMail }
        }
    }

    private fun getPartnersOffers(mail: String, password: String): List<OfferResponse> {
        return httpRequest(
            path = "/api/hrpartners/offers",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail, password))
        ).let {
            it.body as List<Map<String, Any>>
            it.body.map {
                it.toOfferResponse()
            }
        }
    }

    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }

    private fun addHrpartner(mail: String): Response {
        return httpRequest(
            path = "/api/hrpartners",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail)),
            body = mapOf(
                "firstName" to hrPartnerFirstName,
                "lastName" to hrPartnerLastName,
                "mail" to hrpartnerMail
            )
        )
    }

    private fun getUsers(): List<User> {
        val response = httpRequest(
            path = "/api/users",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as List<Map<String, Any>>
            return it.map { it.toUser() }
        }
    }

    private fun addOrganizationAndVerify(): Response {
        httpRequest(
            path = "/api/organizations",
            method = HttpMethod.POST,
            body = mapOf(
                "name" to name,
                "mail" to organizationMail,
                "password" to password,
            )
        )
        val organization = getAddedOrganization()
        val response = httpRequest(
            path = "/_admin/verify/${organization!!.id}",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.adminApiKey to API_KEY)
        )
        return response
    }
    private fun getAddedOrganization() =
        httpRequest(path="/api/organizations", method=HttpMethod.GET).let {
            it.body as List<Map<String, Any>>
            it.body.map { it.toOrganization() }
        }.firstOrNull { it.user.mail == organizationMail }

    private fun getHrPartners(): List<HrPartner> {
        val response = httpRequest(
            path = "/api/hrpartners",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let {
            it as MutableIterable<Map<String, Any>>
            it.map {
                it as Map<String, Any>
                it.toHrPartner()
            }.toList()
        }
    }

    private fun loginUser(userMail: String = hrpartnerMail, userPassword: String = password): Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getAuthToken(mail:String = hrpartnerMail, userPassword: String = password):String =
        loginUser(mail, userPassword).headers?.get(EStellaHeaders.authToken)?.get(0)?:""

    private val name = "name"
    private val organizationMail = "organization@hrpartner.pl"
    private val hrPartnerFirstName = "John"
    private val hrPartnerLastName = "Doe"
    private val hrpartnerMail = "examplemail@hrpartner.pl"
    private val password = "123"

    private val legitHrPartner = hrPartners[1]
    private val legitHrPartnerPassword = "a"

    private val API_KEY = "API_KEY"

    private val randomMail = "randommail@user.pl"
    private val randomPassword = "random-password"

    private val newName = "newName"

}
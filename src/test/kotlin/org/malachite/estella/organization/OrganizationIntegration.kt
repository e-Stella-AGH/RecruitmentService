package org.malachite.estella.organization

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.loader.FakeOrganizations
import org.malachite.estella.commons.loader.FakeUsers.organizationUsers
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.people.domain.HrPartnerResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class OrganizationIntegration : BaseIntegration() {

    @Test
    @Order(1)
    fun `should add organization to database`() {
        val response = addOrganization()
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val users = getUsers()
        val organizations = getOrganizations()
        expectThat(users.find { it.mail == mail }).isNotNull()
        expectThat(organizations.find {it.name == name}).isNotNull()
    }

    @Test
    @Order(2)
    fun `should return Bad Request with corresponding message, when organization already exists`() {
        val response = addOrganization()
        withStatusAndMessage(response, "Address already in use!", HttpStatus.BAD_REQUEST)
    }

    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }

    @Test
    @Order(3)
    fun `should update created Organization`() {
        val organization = getOrganizations().find { it.user.mail == mail }!!

        val response = updateOrganization(organization.id!!)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedOrganization = getOrganizationById(organization.id!!)
        expectThat(updatedOrganization.name == newName)
    }

    @Test
    @Order(4)
    fun `should return Bad Request with message when organization wasn't found`() {
        val response = updateOrganization(UUID.randomUUID())
        withStatusAndMessage(response, "No resource with such id", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(5)
    fun `should return ok with list of hrpartners`() {
        val response = getOrganizationsPartners(legitOrganizationUser.mail, legitOrganizationPassword)
        response.forEach{
            it.let {
                expectThat(it.organizationName).isEqualTo(legitOrganizationName)
            }
        }
    }

    private fun getOrganizationsPartners(mail: String, password: String): List<HrPartnerResponse> {
        return httpRequest(
            path = "/api/organizations/hrpartners",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail, password))
        ).let { response ->
            response.body as List<Map<String, Any>>
            response.body.map {
                it.toHrPartnerResponse()
            }
        }
    }

    @Test
    @Order(6)
    fun `should return ok with list of offers`() {
        val response = getOrganizationsOffers(legitOrganizationUser.mail, legitOrganizationPassword)
        response.forEach{
            it.let {
                expectThat(it.organization.name).isEqualTo(legitOrganizationName)
            }
        }
    }

    private fun getOrganizationsOffers(mail: String, password: String): List<OfferResponse> {
        return httpRequest(
            path = "/api/organizations/offers",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail, password))
        ).let { response ->
            response.body as List<Map<String, Any>>
            response.body.map {
                it.toOfferResponse()
            }
        }
    }

    @Test
    @Order(7)
    fun `should be able to get organization by jwt user`() {
        val response = getOrganizationByUser(legitOrganizationUser.mail, legitOrganizationPassword)
        expectThat(response.name).isEqualTo(legitOrganizationName)
    }

    @Test
    @Order(8)
    fun `should delete organization`() {
        val organization = getOrganizations().find { it.user.mail == mail }!!

        val response = httpRequest(
            path = "/api/organizations/${organization.id.toString()}",
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            method = HttpMethod.DELETE
        )
        withStatusAndMessage(response, "Success", HttpStatus.OK)

        val deletedUserResponse = getOrganizationAsResponse(organization.id!!)
        withStatusAndMessage(deletedUserResponse, "No resource with such id", HttpStatus.BAD_REQUEST)
    }

    private fun addOrganization(): Response {
        return httpRequest(
            path = "/api/organizations",
            method = HttpMethod.POST,
            body = mapOf(
                "name" to name,
                "mail" to mail,
                "password" to password,
                "verified" to false
            )
        )
    }

    private fun updateOrganization(uuid: UUID): Response {
        return httpRequest(
            path = "/api/organizations/$uuid",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            body = mapOf(
                "name" to newName,
                "mail" to mail,
                "password" to password,
                "verified" to false
            )
        )
    }

    private fun getOrganizations():List<Organization> {
        val response = httpRequest(
            path = "/api/organizations",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as List<Map<String, Any>>
            return it.map { it.toOrganization() }
        }
    }

    private fun getOrganizationById(organizationId: UUID): Organization {
        val response = getOrganizationAsResponse(organizationId)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as Map<String, Any>
            return it.toOrganization()
        }
    }

    private fun getOrganizationByUser(userMail: String, userPassword: String): Organization {
        val response = httpRequest(
            path = "/api/organizations/organization",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(userMail, userPassword))
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as Map<String, Any>
            return it.toOrganization()
        }
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

    private fun getOrganizationAsResponse(organizationId: UUID): Response {
        return httpRequest(
            path = "/api/organizations/$organizationId",
            method = HttpMethod.GET
        )
    }

    private fun loginUser(userMail: String = mail, userPassword: String = password): Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getAuthToken():String =
        loginUser().headers!![EStellaHeaders.authToken]!![0]

    private fun getAuthToken(mail: String, password: String) =
        loginUser(mail, password).headers?.get(EStellaHeaders.authToken)?.get(0)?:""

    private val name = "name"
    private val mail = "examplemail@organization.pl"
    private val password = "123"

    private val randomMail = "randommail@user.pl"
    private val randomPassword = "random-password"

    private val legitOrganizationUser = organizationUsers[0]
    private val legitOrganizationName = FakeOrganizations.getCompanies(organizationUsers)[0].name
    private val legitOrganizationPassword = "a"

    private val newName = "newName"

}
package org.malachite.estella.people

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.util.EmailServiceStub
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HrPartnerIntegration : BaseIntegration() {

    @Test
    @Order(1)
    fun `should add hrPartner to database`() {
        EmailServiceStub.stubForSendEmail()
        addOrganization()
        val response = addHrpartner(organizationMail)
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val users = getUsers()
        expectThat(users.find { it.mail == organizationMail }).isNotNull()
    }

    @Test
    @Order(2)
    fun `should not add hrpartner, because not login as organization`() {
        val response = addHrpartner(hrpartnerMail)
        withStatusAndMessage(response, "Unauthenticated", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(3)
    fun `should return Bad Request with corresponding message, when user already exists`() {
        val response = addHrpartner(organizationMail)
        withStatusAndMessage(response, "Address already in use!", HttpStatus.BAD_REQUEST)
    }

    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }

    private fun addHrpartner(mail: String): Response {
        return httpRequest(
            path = "/api/hrpartners/addHrPartner",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail)),
            body = mapOf(
                "mail" to hrpartnerMail,
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

    private fun addOrganization(): Response {
        return httpRequest(
            path = "/api/organizations/addorganization",
            method = HttpMethod.POST,
            body = mapOf(
                "name" to name,
                "mail" to organizationMail,
                "password" to password,
            )
        )
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

    private fun getAuthToken(mail:String = hrpartnerMail):String =
        loginUser(mail).headers?.get(EStellaHeaders.authToken)?.get(0)?:""

    private val name = "name"
    private val organizationMail = "organization@hrpartner.pl"
    private val hrpartnerMail = "examplemail@hrpartner.pl"
    private val password = "123"

    private val randomMail = "randommail@user.pl"
    private val randomPassword = "random-password"

    private val newName = "newName"

}
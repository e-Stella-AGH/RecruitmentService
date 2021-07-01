package org.malachite.estella.organization

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
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
    fun `should delete organization`() {
        val organization = getOrganizations().find { it.user.mail == mail }!!
        println(organization)
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
            path = "/api/organizations/addorganization",
            method = HttpMethod.POST,
            body = mapOf(
                "name" to name,
                "email" to mail,
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
                "email" to mail,
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

    private fun Map<String, Any>.toUser() =
        User(
            this["id"] as Int?,
            this["firstName"] as String,
            this["lastName"] as String,
            this["mail"] as String,
            this["password"] as String?
        )

    private fun Map<String, Any>.toOrganization() =
        Organization(
            UUID.fromString(this["id"] as String),
            this["name"] as String,
            (this["user"] as Map<String,Any>).toUser(),
            this["verified"] as Boolean
        )

    private val name = "name"
    private val mail = "examplemail@user.pl"
    private val password = "123"

    private val randomMail = "randommail@user.pl"
    private val randomPassword = "random-password"

    private val newName = "newName"

}
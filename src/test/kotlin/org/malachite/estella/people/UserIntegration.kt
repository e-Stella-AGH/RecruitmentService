package org.malachite.estella.people

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.User
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserIntegration : BaseIntegration() {

    @Test
    @Order(1)
    fun `should add user to database`() {
        val response = addUser()
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val users = getUsers()
        expectThat(users.find { it.mail == mail }).isNotNull()
    }

    @Test
    @Order(2)
    fun `should return Bad Request with corresponding message, when user already exists`() {
        val response = addUser()
        withStatusAndMessage(response, "Address already in use!", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(3)
    fun `should login user, when account was created`() {
        val response = loginUser()
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as Map<String, String>
        expectThat(response.headers).isNotNull()
        expectThat(response.headers!![EStellaHeaders.refreshToken]).isNotNull()
        expectThat(response.headers[EStellaHeaders.authToken]).isNotNull()
    }

    @Test
    @Order(4)
    fun `should return Bad Request when user login doesn't exist`() {
        val response = loginUser(userMail = randomMail)
        withStatusAndMessage(response, "User with such email: $randomMail not found", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(5)
    fun `should return Bad Request when user password doesn't match`() {
        val response = loginUser(userPassword = randomPassword)
        withStatusAndMessage(response, "Invalid password", HttpStatus.BAD_REQUEST)
    }

    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }

    @Test
    @Order(6)
    fun `should update created User`() {
        val user = getUsers().find { it.mail == mail }!!

        val response = updateUser(user.id!!)

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedUser = getUserById(user.id!!)
        expectThat(updatedUser.firstName == newName)
    }

    @Test
    @Order(7)
    fun `should return Bad Request with message that user is unauthenticated`() {
        val response = updateUser(1000000)
        withStatusAndMessage(response, "Unauthenticated", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(8)
    fun `should delete user`() {
        val user = getUsers().find { it.mail == mail }!!
        val response = httpRequest(
            path = "/api/users/${user.id}",
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            method = HttpMethod.DELETE
        )
        withStatusAndMessage(response, "Success", HttpStatus.OK)

        val deletedUserResponse = getUserAsResponse(user.id!!)
        withStatusAndMessage(deletedUserResponse, "There is no such user", HttpStatus.BAD_REQUEST)
    }

    private fun addUser(): Response {
        return httpRequest(
            path = "/api/users/adduser",
            method = HttpMethod.POST,
            body = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "mail" to mail,
                "password" to password
            )
        )
    }

    private fun updateUser(id: Int): Response {
        return httpRequest(
            path = "/api/users/$id",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            body = mapOf(
                "firstName" to newName,
                "lastName" to lastName,
                "mail" to mail,
                "password" to password
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

    private fun getUserById(userId: Int): User {
        val response = getUserAsResponse(userId)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as Map<String, Any>
            return it.toUser()
        }
    }

    private fun getUserAsResponse(userId: Int): Response {
        return httpRequest(
            path = "/api/users/$userId",
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


    private val firstName = "name"
    private val lastName = "surname"
    private val mail = "examplemail@user.pl"
    private val password = "123"

    private val randomMail = "randommail@user.pl"
    private val randomPassword = "random-password"

    private val newName = "newName"

}
package org.malachite.estella.people

import com.beust.klaxon.Klaxon
import io.netty.handler.codec.base64.Base64Decoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.util.EmailServiceStub
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isNotNull
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserIntegration : BaseIntegration() {

    @Test
    @Order(1)
    fun `should add user to database`() {
        EmailServiceStub.stubForSendEmail()
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
            path = "/api/jobseekers/${user.id}",
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            method = HttpMethod.DELETE
        )
        withStatusAndMessage(response, "Success", HttpStatus.OK)

        val deletedUserResponse = getUserAsResponse(user.id!!)
        withStatusAndMessage(deletedUserResponse, "There is no such user", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(9)
    fun `should return user type of job seeker in jwt`() {
        val decoded = getJWTFor("carthago@delenda.est")
        expect {
            that(decoded.firstName).isEqualTo("Marcus")
            that(decoded.lastName).isEqualTo("Cato")
            that(decoded.mail).isEqualTo("carthago@delenda.est")
            that(decoded.userType).isEqualTo("job_seeker")
        }
    }

    @Test
    @Order(10)
    fun `should return user type of hr in jwt`() {
        val decoded = getJWTFor("principus@roma.com")
        expect {
            that(decoded.firstName).isEqualTo("Octavian")
            that(decoded.lastName).isEqualTo("Augustus")
            that(decoded.mail).isEqualTo("principus@roma.com")
            that(decoded.userType).isEqualTo("hr")
        }
    }

    private fun getJWTFor(mail: String): UserDataFromJWT {
        val response = httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to mail,
                "password" to "a"
            )
        )
        val authHeader = response.headers?.get("X-Auth-Token")
        expectThat(authHeader).isNotNull()
        val decoded = decodeJwt(authHeader?.get(0) ?: "")
        expectThat(decoded).isNotNull()
        return decoded!!
    }

    private fun decodeJwt(jwt: String): UserDataFromJWT? {
        val parts = jwt.split(".")
        expectThat(parts.size).isGreaterThanOrEqualTo(2)
        return Klaxon().parse(String(Base64.getDecoder().decode(parts[1])))
    }

    private data class UserDataFromJWT(
        val firstName: String,
        val lastName: String,
        val userType: String,
        val mail: String
    )

    private fun addUser(): Response {
        return httpRequest(
            path = "/api/jobseekers",
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
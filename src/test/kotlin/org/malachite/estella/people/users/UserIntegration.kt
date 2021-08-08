package org.malachite.estella.people.users

import com.beust.klaxon.Klaxon
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.security.Authority
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
        val response = updateUser()
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedUser = getLoggedInUser()
        expectThat(updatedUser.firstName).isEqualTo(newName)
    }

    @Test
    @Order(7)
    fun `should return Bad Request with message that user is unauthenticated`() {
        val response = updateUser("123456")
        withStatusAndMessage(response, "Unauthenticated", HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(8)
    fun `should return user type of job seeker in jwt`() {
        val decoded = getJWTFor("carthago@delenda.est")
        expect {
            that(decoded.firstName).isEqualTo("Marcus")
            that(decoded.lastName).isEqualTo("Cato")
            that(decoded.mail).isEqualTo("carthago@delenda.est")
            that(decoded.userType).isEqualTo(Authority.job_seeker.name)
        }
    }

    @Test
    @Order(9)
    fun `should return user type of hr in jwt`() {
        val decoded = getJWTFor("alea@iacta.est")
        expect {
            that(decoded.firstName).isEqualTo("Gaius")
            that(decoded.lastName).isEqualTo("Caesar")
            that(decoded.mail).isEqualTo("alea@iacta.est")
            that(decoded.userType).isEqualTo(Authority.hr.name)
        }
    }

    @Test
    @Order(10)
    fun `expired jwt token`() {
        val response = getUserAsResponse(expiredJWT)
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
//        expectThat((response.body as Map<String,Any>)["message"]).isEqualTo("Unauthenticated")
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

    private fun updateUser(authToken:String = getAuthToken()): Response {
        return httpRequest(
            path = "/api/users",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to authToken),
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

    private fun getLoggedInUser(): User {
        val response = getUserAsResponse()
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as Map<String, Any>
            return it.toUser()
        }
    }

    private fun getUserAsResponse(jwt:String = getAuthToken()): Response {
        return httpRequest(
            path = "/api/users/loggedInUser",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to jwt)
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

    private val expiredJWT = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiI1IiwiaWF0IjoxNjI2NTI2NjY3LCJleHAiOjE2MjY1Mjc1NjcsIm1haWwiOiJwcmluY2lwdXNAcm9tYS5jb20iLCJmaXJzdE5hbWUiOiJPY3RhdmlhbiIsImxhc3ROYW1lIjoiQXVndXN0dXMiLCJ1c2VyVHlwZSI6ImhyIn0.DN9JnPkTzvlHFOOH8zjYrXkeenY4qUYDnoy5o320ZcIQkgwDVVqfNd4LEzlEtGxbStjaG_XcykvJsLtT3guIZg"

}
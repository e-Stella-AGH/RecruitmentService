package org.malachite.estella.people.api

import org.malachite.estella.commons.*
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UserController(
    @Autowired private val userService: UserService,
    @Autowired private val securityService: SecurityService
) {

    private val loginExposedHeaders: String =
        arrayOf(EStellaHeaders.authToken, EStellaHeaders.refreshToken).joinToString(", ")

    @CrossOrigin
    @GetMapping
    fun getUsers(): ResponseEntity<MutableIterable<User>> {
        return ResponseEntity(userService.getUsers(), HttpStatus.OK)
    }

    @CrossOrigin
    @PostMapping("/login")
    fun loginUser(@RequestBody body: LoginRequest): ResponseEntity<Message> {
        val user = userService.getUserByEmail(body.mail)
            ?: return ResponseEntity(
                Message("User with such email: ${body.mail} not found"),
                HttpStatus.BAD_REQUEST
            )

        if (!user.comparePassword(body.password))
            return ResponseEntity(Message("Invalid password"), HttpStatus.BAD_REQUEST)

        val tokens = securityService.getTokens(user)
        return tokens?.let {
            ResponseEntity.ok()
                .header(EStellaHeaders.authToken, it.first)
                .header(EStellaHeaders.refreshToken, it.second)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, loginExposedHeaders)
                .body(Message("Success"))
        } ?: ResponseEntity(Message("Error while creating token"), HttpStatus.INTERNAL_SERVER_ERROR)
    }


    @CrossOrigin
    @GetMapping("/loggedInUser")
    fun getLoggedInUser(): ResponseEntity<Any> {
        val user = securityService.getUserFromContext() ?: throw UnauthenticatedException()
        return ResponseEntity.ok(user)
    }

    @CrossOrigin
    @PostMapping("/{userId}/refreshToken")
    fun refresh(
        @PathVariable userId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?
    ): ResponseEntity<Any> {
        jwt ?: return OwnResponses.UNAUTH
        return securityService.refreshToken(jwt, userId)
            ?.let {
                ResponseEntity.ok()
                    .header(EStellaHeaders.authToken, it)
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, loginExposedHeaders)
                    .body(SuccessMessage)
            }
            ?: ResponseEntity.status(404).body((Message("Failed during refreshing; user not found")))
    }


    @CrossOrigin
    @PutMapping
    fun updateUser(
        @RequestBody userRequest: UserRequest
    ): ResponseEntity<Any> =
        userService.updateUser(userRequest)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/personalData")
    fun updatePersonalData(
        @RequestBody personalDataRequest: PersonalDataRequest
    ): ResponseEntity<Any> =
        userService.updateUserPersonalData(personalDataRequest)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @PutMapping("/password")
    fun updatePersonalData(
        @RequestBody passwordRequest: PasswordRequest
    ): ResponseEntity<Any> =
        userService.updateUserPassword(passwordRequest)
            .let { OwnResponses.SUCCESS }

}

data class UserRequest(
    val firstName: String,
    val lastName: String,
    val mail: String,
    val password: String
)

data class PersonalDataRequest(
    val firstName: String,
    val lastName: String,
)

data class PasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)

data class LoginRequest(val mail: String, val password: String)

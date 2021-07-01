package org.malachite.estella.people.api

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.InvalidLoginResponseDto
import org.malachite.estella.people.domain.LoginResponse
import org.malachite.estella.people.domain.LoginResponseDto
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/api/users")
class UserController(
    @Autowired private val userService: UserService,
    @Autowired private val securityService: SecurityService
) {

    @CrossOrigin
    @GetMapping()
    fun getUsers(): ResponseEntity<MutableIterable<User>> {
        return ResponseEntity(userService.getUsers(), HttpStatus.OK)
    }

    @CrossOrigin
    @PostMapping("/login")
    fun loginUser(@RequestBody body: LoginRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        val user = userService.getUserByEmail(body.mail)
            ?: return ResponseEntity(
                InvalidLoginResponseDto("User with such email: ${body.mail} not found"),
                HttpStatus.BAD_REQUEST
            )

        if (!user.comparePassword(body.password))
            return ResponseEntity(InvalidLoginResponseDto("Invalid password"), HttpStatus.BAD_REQUEST)

        val token = securityService.getTokens(user, response)
        return token?.let { ResponseEntity(LoginResponseDto("OK"), HttpStatus.OK) }
            ?: ResponseEntity(InvalidLoginResponseDto("Error while creating token"), HttpStatus.INTERNAL_SERVER_ERROR)
    }


    @CrossOrigin
    @GetMapping("/loggedInUser")
    fun getLoggedInUser(@CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        val user = securityService.getUserFromJWT(jwt)
        return user?.let {
            ResponseEntity.ok(user)
        } ?: ResponseEntity.status(401).body("Unauthenticated")
    }

    @CrossOrigin
    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<String> {
        securityService.deleteCookie(response)
        return ResponseEntity.ok("Success")
    }

    @CrossOrigin
    @PostMapping("/refreshToken")
    fun refresh(
        @RequestBody token: String, @CookieValue("jwt") jwt: String?,
        response: HttpServletResponse
    ): ResponseEntity<String> {

        return securityService.refreshToken(token, jwt, response)
            ?.let { ResponseEntity.ok("Success") }
            ?: ResponseEntity.status(404).body("Failed during refreshing not found user")
    }

    @CrossOrigin
    @PostMapping("/adduser")
    fun addUser(@RequestBody user: UserRequest): ResponseEntity<LoginResponseDto> =
        userService.addUser(user.toUser())
            .let {
                ResponseEntity(LoginResponseDto("User Registered"), HttpStatus.OK)
            }

    @CrossOrigin
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Int): ResponseEntity<User> =
        ResponseEntity(userService.getUser(userId), HttpStatus.OK)


    @CrossOrigin
    @PutMapping("/{userId}")
    fun updateUser(@PathVariable("userId") userId: Int, @RequestBody user: UserRequest): ResponseEntity<User> {
        userService.updateUser(userId, user.toUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @CrossOrigin
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable("userId") userId: Int): ResponseEntity<User> {
        userService.deleteUser(userId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}

data class UserRequest(val firstName: String, val lastName: String, val mail: String, val password: String) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}

data class LoginRequest(val mail: String, val password: String)
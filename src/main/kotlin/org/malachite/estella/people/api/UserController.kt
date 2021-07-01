package org.malachite.estella.people.api

import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OneStringValueMessage
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.User
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
    fun loginUser(@RequestBody body: LoginRequest, response: HttpServletResponse): ResponseEntity<OneStringValueMessage> {
        val user = userService.getUserByEmail(body.mail)
            ?: return ResponseEntity(
                Message("User with such email: ${body.mail} not found"),
                HttpStatus.BAD_REQUEST
            )

        if (!user.comparePassword(body.password))
            return ResponseEntity(Message("Invalid password"), HttpStatus.BAD_REQUEST)

        val token = securityService.getTokens(user, response)
        return token?.let { ResponseEntity(Token(token), HttpStatus.OK) }
            ?: ResponseEntity(Message("Error while creating token"), HttpStatus.INTERNAL_SERVER_ERROR)
    }


    @CrossOrigin
    @GetMapping("/loggedInUser")
    fun getLoggedInUser(@CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        val user = securityService.getUserFromJWT(jwt)
        return user?.let {
            ResponseEntity(user, HttpStatus.OK)
        } ?: ResponseEntity(Message("Unauthenticated"), HttpStatus.UNAUTHORIZED)
    }

    @CrossOrigin
    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Message> {
        securityService.deleteCookie(response)
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

    @CrossOrigin
    @PostMapping("/refreshToken")
    fun refresh(@RequestBody token: String,@CookieValue("jwt") jwt: String?,
                response: HttpServletResponse): ResponseEntity<Message> {

        return securityService.refreshToken(token,jwt, response)
            ?.let { ResponseEntity.ok(SuccessMessage) }
            ?: ResponseEntity.status(404).body(Message("Failed during refreshing not found user"))
    }

    @CrossOrigin
    @PostMapping("/adduser")
    fun addUser(@RequestBody user: UserRequest): ResponseEntity<Message> =
        userService.addUser(user.toUser())
            .let {
                ResponseEntity(Message("User Registered"), HttpStatus.CREATED)
            }

    @CrossOrigin
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Int): ResponseEntity<User> =
        ResponseEntity(userService.getUser(userId), HttpStatus.OK)


    @CrossOrigin
    @PutMapping("/{userId}")
    fun updateUser(@PathVariable("userId") userId: Int, @RequestBody user: UserRequest): ResponseEntity<Message> {
        userService.updateUser(userId, user.toUser())
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

    @CrossOrigin
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable("userId") userId: Int): ResponseEntity<Message> {
        userService.deleteUser(userId)
        return ResponseEntity(SuccessMessage, HttpStatus.OK)
    }

}

data class UserRequest(val firstName: String, val lastName: String, val mail: String, val password: String) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}

data class LoginRequest(val mail: String, val password: String)
data class Token(val token: String): OneStringValueMessage()
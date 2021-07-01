package org.malachite.estella.people.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.OwnResponses
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
                .body(Message("Success"))
    }


    @CrossOrigin
    @GetMapping("/loggedInUser")
    fun getLoggedInUser(@RequestHeader(EStellaHeaders.jwtToken) jwt: String?): ResponseEntity<Any> {
        val user = securityService.getUserFromJWT(jwt) ?: OwnResponses.UNAUTH
        return ResponseEntity.ok(user)
    }

    @CrossOrigin
    @PostMapping("/{userId}/refreshToken")
    fun refresh(
        @PathVariable userId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?
    ): ResponseEntity<Message> {
        jwt ?: return OwnResponses.UNAUTH
        return securityService.refreshToken(jwt, userId)
            ?.let { ResponseEntity.ok().header(EStellaHeaders.authToken, it).body(SuccessMessage) }
            ?: ResponseEntity.status(404).body((Message("Failed during refreshing not found user")))
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
    fun updateUser(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("userId") userId: Int,
        @RequestBody user: UserRequest
    ): ResponseEntity<Message> {
        if (!securityService.checkUserRights(jwt, userId)) return OwnResponses.UNAUTH
        userService.updateUser(userId, user.toUser())
        return OwnResponses.SUCCESS
    }

    @CrossOrigin
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @PathVariable("userId") userId: Int
    ): ResponseEntity<Message> {
        if (!securityService.checkUserRights(jwt, userId)) return OwnResponses.UNAUTH
        userService.deleteUser(userId)
        return ResponseEntity(SuccessMessage,HttpStatus.OK)
    }

}

data class UserRequest(
    val firstName: String,
    val lastName: String,
    val mail: String,
    val password: String
) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}

data class LoginRequest(val mail: String, val password: String)
data class Token(val token: String): OneStringValueMessage()

package org.malachite.estella.people.api

import io.jsonwebtoken.Jwts
import org.malachite.estella.commons.OwnHeaders
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.servlet.http.Cookie
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
    fun loginUser(@RequestBody body: LoginRequest): ResponseEntity<String> {
        val user = userService.getUserByEmail(body.mail)
            ?: return ResponseEntity.badRequest().body("User with such email: ${body.mail} not found")

        if (!user.comparePassword(body.password))
            return ResponseEntity.badRequest().body("Invalid password")

        val tokens = securityService.getTokens(user)
        return tokens?.let {
            ResponseEntity.ok()
                .header(OwnHeaders.authToken, it.first)
                .header(OwnHeaders.refreshToken, it.second)
                .body("Success")
        }
            ?: ResponseEntity.badRequest().body("Error with generating token")
    }


    @CrossOrigin
    @GetMapping("/loggedInUser")
    fun getLoggedInUser(@RequestHeader(OwnHeaders.jwtToken) jwt: String?): ResponseEntity<Any> {
        val user = securityService.getUserFromJWT(jwt) ?: OwnResponses.UNAUTH
        return ResponseEntity.ok(user)
    }

    @CrossOrigin
    @PostMapping("/{userId}/refreshToken")
    fun refresh(@PathVariable userId: Int, @RequestHeader(OwnHeaders.jwtToken) jwt: String?): ResponseEntity<String> {
        jwt?:return OwnResponses.UNAUTH
        return securityService.refreshToken(jwt, userId)
            ?.let { ResponseEntity.ok().header(OwnHeaders.authToken, it).body("Success") }
            ?: ResponseEntity.status(404).body("Failed during refreshing not found user")
    }

    @CrossOrigin
    @PostMapping("/adduser")
    fun addUser(@RequestBody user: UserRequest): ResponseEntity<User> {
        val saved: User = userService.addUser(user.toUser())

        return ResponseEntity.created(URI("/api/users/" + saved.id)).build()
    }

    @CrossOrigin
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Int): ResponseEntity<User> =
        ResponseEntity(userService.getUser(userId), HttpStatus.OK)


    @CrossOrigin
    @PutMapping("/{userId}")
    fun updateUser(
        @RequestHeader(OwnHeaders.jwtToken) jwt: String?,
        @PathVariable("userId") userId: Int,
        @RequestBody user: UserRequest
    ): ResponseEntity<String> {
        if(!securityService.checkUserRights(jwt,userId))return OwnResponses.UNAUTH
        userService.updateUser(userId, user.toUser())
        return ResponseEntity.ok("Success")
    }

    @CrossOrigin
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @RequestHeader(OwnHeaders.jwtToken) jwt: String?,
        @PathVariable("userId") userId: Int
    ): ResponseEntity<String> {
        if(!securityService.checkUserRights(jwt,userId))return OwnResponses.UNAUTH
        userService.deleteUser(userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Success")
    }

}

data class UserRequest(val firstName: String, val lastName: String, val mail: String, val password: String) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}

data class LoginRequest(val mail: String, val password: String)
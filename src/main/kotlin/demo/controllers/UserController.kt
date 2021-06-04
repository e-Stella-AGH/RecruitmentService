package demo.controllers

import demo.models.people.User
import demo.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
@RequestMapping("/api/users")
class UserController(@Autowired private val userService: UserService) {


    @GetMapping()
    fun getUsers(): ResponseEntity<MutableIterable<User>> {
        return ResponseEntity(userService.getUsers(), HttpStatus.OK)
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Int): ResponseEntity<User> {
        val user: User = userService.getUser(userId)

        return ResponseEntity(user, HttpStatus.OK)
    }

    @PostMapping("/adduser")
    fun addUser(@RequestBody user: UserRequest): ResponseEntity<User> {
        val saved: User = userService.addUser(user.toUser())

        return ResponseEntity.created(URI("/api/users/" + saved.id)).build()
    }

    @PutMapping("/{userId}")
    fun updateUser(@PathVariable("userId") userId: Int, @RequestBody user: UserRequest): ResponseEntity<User> {
        userService.updateUser(userId, user.toUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable("userId") userId: Int): ResponseEntity<User> {
        userService.deleteUser(userId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

data class UserRequest(val firstName: String, val lastName: String, val mail: String, val password: String) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}
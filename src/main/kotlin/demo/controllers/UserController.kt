package demo.services

import demo.models.people.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/users")
class UserController(@Autowired private val userService: UserService) {


    @GetMapping("/all")
    fun getUsers(): MutableIterable<User> =
        userService.getUsers()
}
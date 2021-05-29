package demo.service

import demo.models.people.User
import demo.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/users")
class UserResource(@Autowired private val userRepository: UserRepository) {


    @GetMapping("/all")
    fun getUsers(): MutableIterable<User> =
           userRepository.findAll()
}
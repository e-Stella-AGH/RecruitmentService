package demo.services

import demo.models.people.User
import demo.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController



@Service
class UserService(@Autowired private val userRepository: UserRepository){

    fun getUsers(): MutableIterable<User> =
        userRepository.findAll()

}
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

    fun getUser(id: Int): User = userRepository.findById(id).get()

    fun addUser(user: User): User = userRepository.save(user)

    fun updateUser(id: Int, user: User) {
        val currUser: User = getUser(id)
        val updated: User = currUser.copy(firstName = user.firstName,
        lastName = user.lastName, mail = user.mail, password = user.password)
    }

    fun deleteUser(id: Int) = userRepository.deleteById(id)
}
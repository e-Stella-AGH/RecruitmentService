package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserAlreadyExistsException
import org.malachite.estella.people.domain.UserNotFoundException
import org.malachite.estella.people.domain.UserRepository
import org.malachite.estella.people.infrastrucutre.HibernateUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*
import kotlin.NoSuchElementException

@Service
class UserService(
    @Autowired private val userRepository: UserRepository
): EStellaService() {

    override val throwable: Exception = UserNotFoundException()

    fun generatePassword(length:Int = 15):String {
        val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return buildString {
            repeat(length) {append(alphanumeric.random())}
        }
    }

    fun getUsers(): MutableIterable<User> =
        userRepository.findAll()

    fun getUser(id: Int): User =
        withExceptionThrower { userRepository.findById(id).get() } as User

    fun addUser(user: User): User =
        try {
            userRepository.save(user)
        } catch (ex: DataIntegrityViolationException) {
            ex.printStackTrace()
            throw UserAlreadyExistsException()
        }

    fun updateUser(id: Int, user: User) {
        val currUser: User = getUser(id)
        val updated: User = currUser.copy(
            firstName = user.firstName,
            lastName = user.lastName, mail = user.mail
        )
        updated.password = user.password
    }

    fun deleteUser(id: Int) = withExceptionThrower { userRepository.deleteById(id) }

    fun getUserByEmail(email: String): User? = userRepository.findByMail(email).orElse(null)

}
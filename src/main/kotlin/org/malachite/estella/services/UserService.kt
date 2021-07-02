package org.malachite.estella.services

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.mails.userRegistrationMailPayload
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
    @Autowired private val userRepository: UserRepository,
    @Autowired private val mailService: MailService
) {
    private fun withUserNotFound(fn: () -> Any) =
        try {
            fn()
        } catch (ex: NoSuchElementException) {
            throw UserNotFoundException()
        }


    fun getUsers(): MutableIterable<User> =
        withUserNotFound { userRepository.findAll() } as MutableIterable<User>

    fun getUser(id: Int): User =
        withUserNotFound { userRepository.findById(id).get() } as User

    fun addUser(user: User): User =
        withUserNotFound {
            try {
                userRepository.save(user)
            } catch (ex: DataIntegrityViolationException) {
                ex.printStackTrace()
                throw UserAlreadyExistsException()
            }
        } as User

    fun updateUser(id: Int, user: User) {
        val currUser: User = getUser(id)
        val updated: User = currUser.copy(
            firstName = user.firstName,
            lastName = user.lastName, mail = user.mail
        )
        updated.password = user.password
    }

    fun deleteUser(id: Int) = withUserNotFound { userRepository.deleteById(id) } as Optional<User>

    fun getUserByEmail(email: String): User? = userRepository.findByMail(email).orElse(null)

    fun registerUser(user:User):User =
        addUser(user).let {
            mailService.sendMail(userRegistrationMailPayload(user))
            user
        }


}
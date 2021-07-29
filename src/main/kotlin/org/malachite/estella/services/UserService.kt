package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.api.UserRequest
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
    @Autowired private val securityService: SecurityService
) : EStellaService<User>() {

    override val throwable: Exception = UserNotFoundException()

    fun generatePassword(length: Int = 15): String {
        val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return buildString {
            repeat(length) { append(alphanumeric.random()) }
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

    fun updateUser(userRequest: UserRequest, jwt: String?) {
        val originalUser = securityService.getUserFromJWT(jwt) ?: throw UnauthenticatedException()
        if (!getPermissions(originalUser.id!!, jwt).contains(Permission.UPDATE)) throw UnauthenticatedException()
        updateUser(originalUser.id, userRequest)
    }

    private fun updateUser(id: Int, user: UserRequest) {
        val currUser: User = getUser(id)
        val updated: User = currUser.copy(
            firstName = user.firstName,
            lastName = user.lastName
        )
        updated.password = user.password
        userRepository.save(updated)
    }

    fun getUserByEmail(email: String): User? = userRepository.findByMail(email).orElse(null)

    private fun getPermissions(id: Int, jwt: String?): Set<Permission> {
        if (securityService.isCorrectApiKey(jwt)) return Permission.allPermissions()
        securityService.getUserFromJWT(jwt)?.let {
            if (it.id == id) return Permission.allPermissions()
            else throw UnauthenticatedException()
        } ?: throw UnauthenticatedException()
    }

}
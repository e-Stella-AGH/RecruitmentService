package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.api.UserRequest
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.domain.*
import org.malachite.estella.security.Authority
import org.malachite.estella.security.UserContextDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class UserService(
    @Autowired private val securityService: SecurityService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val hrPartnerRepository: HrPartnerRepository,
    @Autowired private val jobSeekerRepository: JobSeekerRepository,
    @Autowired private val organizationRepository: OrganizationRepository
): EStellaService<User>() {

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
        // TODO [ES-187] - It won't work because of encryption on setter (it invokes encryption of encrypted password)
        updated.password = user.password
        userRepository.save(updated)
    }

    fun getUserType(id: Int): Authority? =
        listOf(
                hrPartnerRepository.findByUserId(id).map { Authority.hr },
                jobSeekerRepository.findByUserId(id).map { Authority.job_seeker },
                organizationRepository.findByUserId(id).map { Authority.organization }
        ).mapNotNull {
            it.orElse(null)
        }.firstOrNull()

    fun getUserByEmail(email: String): User? = userRepository.findByMail(email).orElse(null)

    fun getUserContextDetails(user: User): UserContextDetails = UserContextDetails(
            user.id!!,
            user.firstName,
            user.lastName,
            user.mail,
            listOfNotNull(getUserType(user.id)),
            true
    )

    private fun getPermissions(id: Int, jwt: String?): Set<Permission> {
        if (securityService.isCorrectApiKey(jwt)) return Permission.allPermissions()
        securityService.getUserFromJWT(jwt)?.let {
            if (it.id == id) return Permission.allPermissions()
            else throw UnauthenticatedException()
        } ?: throw UnauthenticatedException()
    }

}
package org.malachite.estella.services

import io.jsonwebtoken.Jwts
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.api.UserRequest
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
        withExceptionThrower { userRepository.findById(id).get() }

    fun addUser(user: User): User =
        try {
            userRepository.save(user)
        } catch (ex: DataIntegrityViolationException) {
            ex.printStackTrace()
            throw UserAlreadyExistsException()
        }

    fun updateUser(userRequest: UserRequest) {
        val originalUser = securityService.getUserFromContext() ?: throw UnauthenticatedException()
        if (!getPermissions(originalUser.id!!).contains(Permission.UPDATE)) throw UnauthenticatedException()

        val currUser: User = getUser(originalUser.id)
        val updated: User = currUser.copy(
            firstName = userRequest.firstName,
            lastName = userRequest.lastName
        )
        updated.password = userRequest.password
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

    fun getUserContextDetails(token: String): UserContextDetails {
        val claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(token)
        val user = getUserByEmail(claims.body["mail"].toString())
        return UserContextDetails(
                user!!,
                token,
                listOfNotNull(getUserType(user.id!!)),
                true
        )
    }

    private fun getPermissions(id: Int): Set<Permission> {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return Permission.allPermissions()

        return userDetails?.user?.let {
            if (it.id == id) Permission.allPermissions()
            else null
        } ?: throw UnauthenticatedException()
    }

}
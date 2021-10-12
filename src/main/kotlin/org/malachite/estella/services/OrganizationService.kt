package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.organization.domain.OrganizationNotFoundException
import org.malachite.estella.organization.domain.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrganizationService(
    @Autowired private val organizationRepository: OrganizationRepository,
    @Autowired private val userService: UserService,
    @Autowired private val mailService: MailService,
    @Autowired private val securityService: SecurityService
) : EStellaService<Organization>() {

    override val throwable: Exception = OrganizationNotFoundException()

    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: UUID): Organization = withExceptionThrower { organizationRepository.findById(id).get() }
    fun getOrganization(id: String): Organization = withExceptionThrower {
        UUID.fromString(id).let { getOrganization(it) }
    }

    fun addOrganization(organization: Organization): Organization {
        val user = userService.addUser(organization.user)
        return organization.copy(user = user).let { organizationRepository.save(it) }
    }

    fun updateOrganization(id: UUID, organization: Organization) {
        if (!checkRights(id).contains(Permission.UPDATE)) throw UnauthenticatedException()
        getOrganization(id).copy(name = organization.name, verified = organization.verified)
            .let { organizationRepository.save(it) }
    }

    fun updateTasks(uuid: String, tasks: Set<Task>) =
        getOrganization(uuid)
            .let {
                val ids = tasks.map { it.id }
                val newTasks = it.tasks.filterNot { ids.contains(it.id) }.toSet()
                it.copy(tasks = newTasks)
            }
            .let { it.copy(tasks = it.tasks.plus(tasks)) }
            .let { organizationRepository.save(it) }


    fun deleteOrganization(id: UUID) {
        if (!checkRights(id).contains(Permission.DELETE)) throw UnauthenticatedException()
        organizationRepository.deleteById(id)
    }

    fun verifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, true)

    fun deverifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, false)

    private fun changeOrganizationVerification(uuid: String, verified: Boolean): Organization = withExceptionThrower {
        val organization = addOrganization(
            getOrganization(UUID.fromString(uuid))
                .copy(verified = verified)
        )

        mailService.sendOrganizationVerificationMail(organization, verified)
        organization
    }


    fun checkRights(id: UUID): Set<Permission> {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return Permission.allPermissions()

        return securityService.getOrganizationFromContext()?.id
            ?.let {
                if (it == id) Permission.allPermissions()
                else null
            } ?: throw UnauthenticatedException()
    }

}

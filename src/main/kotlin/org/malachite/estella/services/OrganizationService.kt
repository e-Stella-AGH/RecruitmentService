package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
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
): EStellaService() {

    override val throwable: Exception = OrganizationNotFoundException()

    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: UUID): Organization = withExceptionThrower { organizationRepository.findById(id).get() } as Organization

    fun addOrganization(organization: Organization): Organization {
        val user = userService.addUser(organization.user)
        return organization.copy(user = user).let { organizationRepository.save(it) }
    }

    fun updateOrganization(id: UUID, organization: Organization, jwt: String?) {
        checkRights(id, jwt)
        updateOrganization(id, organization)
    }

    private fun updateOrganization(id: UUID, organization: Organization) =
        getOrganization(id).copy(name = organization.name, verified = organization.verified).let { organizationRepository.save(it) }

    fun getOrganizationByUser(user: User): Organization =
        withExceptionThrower { (user.id
            ?.let { organizationRepository.findByUserId(it) }
            ?: Optional.empty<Organization>())
            .get() } as Organization

    fun deleteOrganization(id: UUID, jwt: String?) {
        checkRights(id, jwt)
        deleteOrganization(id)
    }

    private fun deleteOrganization(id: UUID) = withExceptionThrower { organizationRepository.deleteById(id) }

    fun verifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, true)

    fun deverifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, false)

    fun changeOrganizationVerification(uuid: String, verified: Boolean): Organization {
        val organization = addOrganization(
            getOrganization(UUID.fromString(uuid))
                .copy(verified = verified)
        )

        mailService.sendOrganizationVerificationMail(organization, verified)
        return organization
    }

    fun checkRights(id: UUID, jwt: String?) {
        val issuerId = securityService.getOrganizationFromJWT(jwt)?.id
        issuerId?.let { if(it != id) throw UnauthenticatedException() } ?: throw UnauthenticatedException()
    }
}

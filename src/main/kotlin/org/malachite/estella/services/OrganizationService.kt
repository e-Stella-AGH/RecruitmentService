package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.Permission
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
): EStellaService<Organization>() {

    override val throwable: Exception = OrganizationNotFoundException()

    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: UUID): Organization = withExceptionThrower { organizationRepository.findById(id).get() }

    fun addOrganization(organization: Organization): Organization {
        val user = userService.addUser(organization.user)
        return organization.copy(user = user).let { organizationRepository.save(it) }
    }

    fun updateOrganization(id: UUID, organization: Organization, jwt: String?) =
        if(!checkRights(id, jwt).contains(Permission.UPDATE)) throw UnauthenticatedException()
        else updateOrganization(id, organization)


    private fun updateOrganization(id: UUID, organization: Organization) =
        getOrganization(id).copy(name = organization.name, verified = organization.verified).let { organizationRepository.save(it) }

    fun deleteOrganization(id: UUID, jwt: String?) =
        if(!checkRights(id, jwt).contains(Permission.DELETE)) throw UnauthenticatedException()
        else deleteOrganization(id)


    private fun deleteOrganization(id: UUID) = organizationRepository.deleteById(id)

    fun verifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, true)

    fun deverifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, false)

    private fun changeOrganizationVerification(uuid: String, verified: Boolean): Organization {
        val organization = addOrganization(
            getOrganization(UUID.fromString(uuid))
                .copy(verified = verified)
        )

        mailService.sendOrganizationVerificationMail(organization, verified)
        return organization
    }

    fun checkRights(id: UUID, jwt: String?): Set<Permission> =
        if (securityService.isCorrectApiKey(jwt)) Permission.allPermissions()
        else securityService.getOrganizationFromJWT(jwt)?.id
            ?.let {
                if(it == id) Permission.allPermissions()
                else throw UnauthenticatedException()
            } ?: throw UnauthenticatedException()

}

package org.malachite.estella.services

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.organization.domain.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrganizationService(
    @Autowired private val organizationRepository: OrganizationRepository,
    @Autowired private val userService: UserService,
    @Autowired private val mailService: MailService
) {
    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: UUID): Organization = organizationRepository.findById(id).get()

    fun addOrganization(organization: Organization): Organization {
        val user = userService.addUser(organization.user)
        return organization.copy(user = user).let { organizationRepository.save(it) }
    }

    fun updateOrganization(id: UUID, organization: Organization) {
        val currOrganization: Organization = getOrganization(id)
        val updated: Organization = currOrganization.copy(name = organization.name, verified = organization.verified)

        organizationRepository.save(updated)
    }

    fun getOrganizationByUser(user: User): Organization =
        organizationRepository.findByUser(user).get()

    fun deleteOrganization(id: UUID) = organizationRepository.deleteById(id)

    fun verifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, true)


    fun deverifyOrganization(uuid: String): Organization =
        changeOrganizationVerification(uuid, false)

    fun changeOrganizationVerification(uuid: String, verified: Boolean): Organization {
        val organization = addOrganization(
            getOrganization(UUID.fromString(uuid))
                .copy(verified = verified)
        )

        mailService.sendMail(mailService.organizationVerificationMailPayload(organization, verified))
        return organization
    }
}

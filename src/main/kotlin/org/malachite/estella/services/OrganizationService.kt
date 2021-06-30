package org.malachite.estella.services

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrganizationService(@Autowired private val organizationRepository: OrganizationRepository) {
    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: UUID): Organization = organizationRepository.findById(id).get()

    fun addOrganization(organization: Organization): Organization = organizationRepository.save(organization)

    fun updateOrganization(id: UUID, organization: Organization) {
        val currOrganization: Organization = getOrganization(id)
        val updated: Organization = currOrganization.copy(name = organization.name, verified = organization.verified)

        organizationRepository.save(updated)
    }

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
        //send mail
        MailService.sendMail(organization.verificationMailPayload(verified))
        return organization
    }

    fun Organization.verificationMailPayload(verified: Boolean) =
        MailPayload(
            subject = "Your company has been ${if(verified) "verified" else "unverified"}!",
            sender_name = "e-Stella Team",
            receiver = "null", //TODO - change, when organization gets its email
            content = if(verified) getVerificationText() else getUnVerificationText(),
            sender_email = "estellaagh@gmail.com"
        )

    fun getVerificationText() =
        """Your company was successfully verified! You can log in now to your account!"""

    fun getUnVerificationText() =
        """We're sorry to inform you that your company was unverified and so your account was disabled. Please, contact us
            at estellaagh@gmail.com to resolve this issue.
        """.trimMargin()
}

package demo.services

import demo.models.people.Organization
import demo.repositories.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrganizationService(@Autowired private val organizationRepository: OrganizationRepository) {
    fun getOrganizations(): MutableIterable<Organization> = organizationRepository.findAll()

    fun getOrganization(id: String): Optional<Organization> = organizationRepository.findById(id)

    fun addOrganization(organization: Organization): Organization = organizationRepository.save(organization)

    fun updateOrganization(id: String, organization: Organization) {
        val currOrganization: Optional<Organization> = organizationRepository.findById(id)
        if (currOrganization.isPresent) {
            val updated: Organization = currOrganization.get().copy(name = organization.name, verified = organization.verified)

            organizationRepository.save(updated)
        }
    }

    fun deleteOrganization(id: String) = organizationRepository.deleteById(id)
}
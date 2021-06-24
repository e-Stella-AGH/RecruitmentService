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
}

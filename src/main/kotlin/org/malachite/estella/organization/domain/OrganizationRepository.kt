package org.malachite.estella.organization.domain

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import java.util.*

interface OrganizationRepository {
    fun findAll(): MutableIterable<Organization>
    fun findById(id: UUID): Optional<Organization>
    fun findByUser(user:User): Optional<Organization>
    fun save(organization: Organization): Organization
    fun deleteById(id: UUID)
}
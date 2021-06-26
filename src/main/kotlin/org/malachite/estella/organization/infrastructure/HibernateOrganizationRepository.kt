package org.malachite.estella.organization.infrastructure

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.organization.domain.OrganizationRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HibernateOrganizationRepository: CrudRepository<Organization, UUID>, OrganizationRepository

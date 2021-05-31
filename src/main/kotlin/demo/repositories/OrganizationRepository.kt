package demo.repositories

import demo.models.people.Organization
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrganizationRepository: CrudRepository<Organization, String> {}
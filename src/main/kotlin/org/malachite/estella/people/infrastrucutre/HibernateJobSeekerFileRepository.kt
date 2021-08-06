package org.malachite.estella.people.infrastrucutre

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.JobSeekerFileRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateJobSeekerFileRepository: CrudRepository<JobSeekerFile, Int>, JobSeekerFileRepository {
}
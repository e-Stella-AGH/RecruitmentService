package org.malachite.estella.aplication.infrastructure;

import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.models.offers.Application
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateApplicationRepository: CrudRepository<Application, Int>, ApplicationRepository {
    override fun getAllByJobSeekerId(jobSeekerId: Int): List<Application>
}

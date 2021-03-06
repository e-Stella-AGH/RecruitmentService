package org.malachite.estella.aplication.infrastructure;

import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateApplicationStageRepository: CrudRepository<ApplicationStageData, Int>, ApplicationStageRepository {
}

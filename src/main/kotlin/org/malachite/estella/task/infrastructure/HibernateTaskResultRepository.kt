package org.malachite.estella.task.infrastructure

import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.task.domain.TaskResultRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateTaskResultRepository: CrudRepository<TaskResult, Int>, TaskResultRepository {
}
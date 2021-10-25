package org.malachite.estella.task.infrastructure

import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateTaskStageRepository: CrudRepository<TaskStage, Int>, TaskStageRepository
package org.malachite.estella.task.infrastructure

import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.task.domain.TaskRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HibernateTaskRepository: CrudRepository<Task, Int>, TaskRepository
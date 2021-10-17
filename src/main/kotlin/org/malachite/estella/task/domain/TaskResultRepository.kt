package org.malachite.estella.task.domain

import org.malachite.estella.commons.models.tasks.TaskResult
import java.util.*

interface TaskResultRepository {
    fun save(result: TaskResult): TaskResult
    fun findAll(): List<TaskResult>
    fun findById(id: Int): Optional<TaskResult>
    fun delete(result: TaskResult)
}
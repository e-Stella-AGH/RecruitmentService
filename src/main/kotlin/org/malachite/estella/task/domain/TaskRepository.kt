package org.malachite.estella.task.domain

import org.malachite.estella.commons.models.tasks.Task
import java.util.*

interface TaskRepository {
    fun save(task: Task): Task
    fun findAll(): List<Task>
    fun findById(id: Int): Optional<Task>
    fun delete(task: Task)
}
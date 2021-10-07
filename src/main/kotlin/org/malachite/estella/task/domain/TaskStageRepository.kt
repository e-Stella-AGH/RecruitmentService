package org.malachite.estella.task.domain

import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskStage
import java.util.*

interface TaskStageRepository {
    fun save(task: TaskStage): TaskStage
    fun findAll(): List<TaskStage>
    fun findById(uuid: UUID): Optional<TaskStage>
    fun delete(task: TaskStage)
}
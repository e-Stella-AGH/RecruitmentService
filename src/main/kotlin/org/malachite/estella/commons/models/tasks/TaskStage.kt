package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.offers.ApplicationStageData
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
    @JsonIgnore @OneToOne val applicationStage: ApplicationStageData
) {

    @JsonIgnore
    @OneToMany(mappedBy = "taskStage", fetch = FetchType.EAGER)
    var tasksResult: Set<TaskResult> = setOf()


    constructor(id: UUID?, tasksResult: Set<TaskResult>, applicationStage: ApplicationStageData) : this(
        id,
        applicationStage
    ) {
        this.tasksResult = tasksResult
    }

    fun copy(
        id: UUID? = this.id,
        tasksResult: Set<TaskResult> = this.tasksResult,
        applicationStage: ApplicationStageData = this.applicationStage
    ): TaskStage =
        TaskStage(id, tasksResult, applicationStage)
}

package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.offers.ApplicationStageData
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID?,

    @JsonIgnore
    @OneToOne
    val applicationStage: ApplicationStageData,

    @JsonIgnore
    @OneToMany(mappedBy = "taskStage", fetch = FetchType.EAGER)
    var tasksResult: Set<TaskResult> = setOf(),
) {
    override fun equals(other: Any?) = other is TaskStage && EssentialData(this) == EssentialData(other)
    override fun hashCode() = EssentialData(this).hashCode()
    override fun toString() = EssentialData(this).toString().replaceFirst("EssentialData", "TaskStage")

    companion object {
        private data class EssentialData(
            val id: UUID?,
            val applicationStage: ApplicationStageData,
        ) {
            constructor(stage: TaskStage) : this(
                stage.id, stage.applicationStage
            )
        }
    }
}

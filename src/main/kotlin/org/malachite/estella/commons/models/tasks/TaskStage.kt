package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.offers.ApplicationStageData
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
    @OneToMany @JoinColumn(name="tasks_stages_id") val tasksResult:Set<TaskResult>,
    @JsonIgnore @OneToOne val applicationStage: ApplicationStageData
)

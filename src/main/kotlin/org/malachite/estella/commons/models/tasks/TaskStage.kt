package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.offers.ApplicationStageData
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
        @JsonIgnore @OneToMany(mappedBy = "taskStage",fetch = FetchType.EAGER) val tasksResult:List<TaskResult>,
        @JsonIgnore @OneToOne val applicationStage: ApplicationStageData,
        @ElementCollection(fetch = FetchType.EAGER) val devs: Set<String>
)

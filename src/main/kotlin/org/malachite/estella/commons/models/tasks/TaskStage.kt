package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.quizes.Question
import org.springframework.transaction.annotation.Transactional
import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
        @OneToMany(mappedBy = "taskStage") val tasksResult:Set<TaskResult>,
        @JsonIgnore @OneToOne val applicationStage: ApplicationStageData
)

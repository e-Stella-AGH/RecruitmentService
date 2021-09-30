package org.malachite.estella.commons.models.tasks

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.quizes.Question
import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "tasks_stages")
data class TaskStage(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @OneToMany @JoinColumn(name="tasks_stages_id") val tasksResult:Set<TaskResult>,
    @OneToOne val interview: Interview?
)

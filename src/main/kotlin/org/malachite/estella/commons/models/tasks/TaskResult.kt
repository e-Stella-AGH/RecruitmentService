package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.transaction.annotation.Transactional
import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "tasks_results")
data class TaskResult(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @Lob val results: Blob, @Lob val code: Clob,
    val startTime: Timestamp?, val endTime: Timestamp?,
    @ManyToOne val task: Task,
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "taskStage") val taskStage: TaskStage
)

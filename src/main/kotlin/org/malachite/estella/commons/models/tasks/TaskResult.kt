package org.malachite.estella.commons.models.tasks

import com.fasterxml.jackson.annotation.JsonIgnore
import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "tasks_results")
data class TaskResult(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @Lob val results: Blob?, @Lob val code: Clob?,
    val startTime: Timestamp?, val endTime: Timestamp?,
    @ManyToOne val task: Task
) {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "taskStage")
    var taskStage: TaskStage? = null

    constructor(
        id: Int?,
        results: Blob?,
        code: Clob?,
        startTime: Timestamp?,
        endTime: Timestamp?,
        task: Task,
        taskStage: TaskStage
    ) : this(id, results, code, startTime, endTime, task) {
        this.taskStage = taskStage
    }

    fun copy(
        id: Int? = this.id,
        results: Blob? = this.results,
        code: Clob? = this.code,
        startTime: Timestamp? = this.startTime,
        endTime: Timestamp? = this.endTime,
        task: Task = this.task,
        taskStage: TaskStage? = this.taskStage
    ): TaskResult = TaskResult(id, results, code, startTime, endTime, task, taskStage!!)

}

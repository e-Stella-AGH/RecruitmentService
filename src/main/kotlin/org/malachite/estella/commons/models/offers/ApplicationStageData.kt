package org.malachite.estella.commons.models.offers

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.interviews.Note
import org.malachite.estella.commons.models.tasks.TaskStage
import javax.persistence.*

@Entity
@Table(name = "application_stages_data")
data class ApplicationStageData(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @ManyToOne val stage: RecruitmentStage,
    @JsonIgnore @ManyToOne @JoinColumn(name = "application_id") val application: Application,
    @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name = "interviews_id") val notes: Set<Note>,
    @ElementCollection(fetch = FetchType.EAGER) val hosts: MutableSet<String>
) {

    @JsonIgnore
    @OneToOne
    var tasksStage: TaskStage? = null

    @JsonIgnore
    @OneToOne
    var interview: Interview? = null

    constructor(
        id: Int?,
        stage: RecruitmentStage,
        application: Application,
        tasksStage: TaskStage?,
        interview: Interview?,
        notes: Set<Note>,
        hosts: MutableSet<String>
    ) : this(id, stage, application, notes, hosts) {
        this.tasksStage = tasksStage
        this.interview = interview
    }


    fun copy(
        id: Int? = this.id,
        stage: RecruitmentStage = this.stage,
        application: Application = this.application,
        tasksStage: TaskStage? = this.tasksStage,
        interview: Interview? = this.interview,
        notes: Set<Note> = this.notes,
        hosts: MutableSet<String> = this.hosts
    ) = ApplicationStageData(id,stage,application,tasksStage,interview,notes,hosts)
}
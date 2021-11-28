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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "application_id")
    val application: Application,

    @JsonIgnore
    @OneToOne
    var tasksStage: TaskStage? = null,

    @JsonIgnore
    @OneToOne
    var interview: Interview? = null,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "interviews_id")
    val notes: Set<Note>,

    @ElementCollection(fetch = FetchType.EAGER) val hosts: MutableSet<String>,
) {

    override fun equals(other: Any?) = other is ApplicationStageData && EssentialData(this) == EssentialData(other)
    override fun hashCode() = EssentialData(this).hashCode()
    override fun toString() = EssentialData(this).toString().replaceFirst("EssentialData", "ApplicationStageData")

    companion object {
        private data class EssentialData(
            val id: Int?,
            val stage: RecruitmentStage,
            val application: Application,
            val notes: Set<Note>,
            val hosts: MutableSet<String>,
        ) {
            constructor(data: ApplicationStageData) : this(
                data.id, data.stage, data.application, data.notes, data.hosts
            )
        }
    }
}
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
        @JsonIgnore @OneToOne val tasksStage: TaskStage?,
        @OneToOne val interview: Interview?,
        @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name = "interviews_id") val notes: Set<Note>
)
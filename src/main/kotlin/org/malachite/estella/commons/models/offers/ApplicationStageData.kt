package org.malachite.estella.commons.models.offers

import com.fasterxml.jackson.annotation.JsonIgnore
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.tasks.TaskStage
import javax.persistence.*

@Entity
@Table(name = "application_stages_data")
data class ApplicationStageData(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @ManyToOne val stage: RecruitmentStage,
    @JsonIgnore @ManyToOne @JoinColumn(name = "application_id") val application: Application,
    @OneToOne val tasksStage: TaskStage?,
    @OneToOne val interview: Interview?
)
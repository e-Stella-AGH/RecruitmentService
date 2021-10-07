package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.commons.models.quizes.QuizResult
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "application")
data class Application(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
    @ManyToOne val jobSeeker: JobSeeker,
    @ManyToMany(cascade = [CascadeType.ALL]) @JoinTable(
        name = "application_files",
        joinColumns = [JoinColumn(name = "application_id")],
        inverseJoinColumns = [JoinColumn(name = "job_seeker_file_id")]
    ) val seekerFiles: MutableSet<JobSeekerFile>,
    @OneToMany(
        mappedBy = "application"
    ) val applicationStages: MutableList<ApplicationStageData>
)

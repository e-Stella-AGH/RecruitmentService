package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.commons.models.quizes.QuizResult
import org.malachite.estella.commons.models.tasks.TaskResult
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "applications")
data class Application(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
        @ManyToOne val stage: RecruitmentStage, @ManyToOne val jobSeeker: JobSeeker,
        @ManyToMany(cascade = [CascadeType.ALL]) @JoinTable(
                name = "application_files",
                joinColumns = [JoinColumn(name = "application_id")],
                inverseJoinColumns = [JoinColumn(name = "job_seeker_file_id")]
        ) val seekerFiles: Set<JobSeekerFile>,
        @OneToMany @JoinColumn(name="application_id") val quizzesResults: Set<QuizResult>,
        @OneToMany @JoinColumn(name="application_id") val tasksResults: Set<TaskResult>,
        @OneToMany @JoinColumn(name="application_id") val interviews: Set<Interview>

)

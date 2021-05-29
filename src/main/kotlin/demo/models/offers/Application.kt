package demo.models.offers

import demo.models.interviews.Interview
import demo.models.people.JobSeeker
import demo.models.people.JobSeekerFile
import demo.models.quizes.QuizResult
import demo.models.tasks.TaskResult
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "applications")
data class Application(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
        @ManyToOne val stage: RecruitmentStage, @ManyToOne val jobSeeker: JobSeeker,
        @ManyToMany @JoinTable(
                name = "application_files",
                joinColumns = [JoinColumn(name = "job_seekers_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val seekerFiles: Set<JobSeekerFile>,
        @OneToMany @JoinColumn(name="applications_id") val quizzesResults: Set<QuizResult>,
        @OneToMany @JoinColumn(name="applications_id") val tasksResults: Set<TaskResult>,
        @OneToMany @JoinColumn(name="applications_id") val interviews: Set<Interview>

)

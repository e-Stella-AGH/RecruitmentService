package demo.models.offers

import demo.models.interviews.Interview
import demo.models.people.JobSeeker
import demo.models.people.JobSeekerFile
import demo.models.quizes.QuizResult
import demo.models.tasks.TaskResult
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "Applications")
data class Application(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val applicationDate: Date, @Enumerated(EnumType.STRING) val status: ApplicationStatus,
        @ManyToOne val stage: RecruitmentStage, @ManyToOne val jobSeeker: JobSeeker,
        @ManyToMany @JoinTable(
                name = "ApplicationFiles",
                joinColumns = [JoinColumn(name = "job_seekers_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val seekerFiles: Set<JobSeekerFile>,
        @ManyToMany @JoinTable(
                name = "ApplicationQuizes",
                joinColumns = [JoinColumn(name = "quizzes_results_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val quizzesResults: Set<QuizResult>,
        @ManyToMany @JoinTable(
                name = "ApplicationTasks",
                joinColumns = [JoinColumn(name = "tasks_results_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val tasksResults: Set<TaskResult>,
        @ManyToMany @JoinTable(
                name = "ApplicationTasks",
                joinColumns = [JoinColumn(name = "interviews_id")],
                inverseJoinColumns = [JoinColumn(name = "applications_id")]
        ) val interviews: Set<Interview>

)

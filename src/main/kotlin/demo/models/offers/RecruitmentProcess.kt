package demo.models.offers

import demo.models.quizes.Quiz
import demo.models.tasks.Task
import java.sql.Date
import javax.persistence.*


@Entity
@Table(name = "RecruitmentProcesses")
data class RecruitmentProcess(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val startDate: Date, val endDate: Date, @OneToOne val offer: Offer,
        @OneToMany val stages: List<RecruitmentStage>,
        @ManyToMany @JoinTable(
                name = "RecruitmentProcessQuizes",
                joinColumns = [JoinColumn(name = "recruitment_processes_id")],
                inverseJoinColumns = [JoinColumn(name = "quiz_id")]
        ) val quizzes: Set<Quiz>,
        @ManyToMany @JoinTable(
                name = "RecruitmentProcessTaskss",
                joinColumns = [JoinColumn(name = "recruitment_processes_id")],
                inverseJoinColumns = [JoinColumn(name = "tasks_id")]
        ) val tasks: Set<Task>,
)

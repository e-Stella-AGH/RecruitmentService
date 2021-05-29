package demo.models.offers

import demo.models.quizes.Quiz
import demo.models.tasks.Task
import java.sql.Date
import javax.persistence.*


@Entity
@Table(name = "recruitment_processes")
data class RecruitmentProcess(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val startDate: Date, val endDate: Date, @OneToOne val offer: Offer,
        @OneToMany @JoinColumn(name="recruitment_processes_id") val stages: List<RecruitmentStage>,
        @ManyToMany @JoinTable(
                name = "recruitment_process_quizes",
                joinColumns = [JoinColumn(name = "recruitment_processes_id")],
                inverseJoinColumns = [JoinColumn(name = "quizes_id")]
        ) val quizzes: Set<Quiz>,
        @ManyToMany @JoinTable(
                name = "recruitment_process_tasks",
                joinColumns = [JoinColumn(name = "recruitment_processes_id")],
                inverseJoinColumns = [JoinColumn(name = "tasks_id")]
        ) val tasks: Set<Task>,
)

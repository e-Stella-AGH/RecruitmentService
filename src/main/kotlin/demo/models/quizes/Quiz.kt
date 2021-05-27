package demo.models.quizes

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "RecruitmentProcesses")
data class Quiz(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String, val deadline:Date,
        @OneToMany val questions:Set<Question>
)

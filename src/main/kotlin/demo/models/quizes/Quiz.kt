package demo.models.quizes

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "quizes")
data class Quiz(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String, val deadline:Date,
        @OneToMany @JoinColumn(name="quiz_id") val questions:Set<Question>
)

package demo.models.quizes

import demo.models.offers.Application
import javax.persistence.*

@Entity
@Table(name = "quiz_results")
data class QuizResult(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @ManyToOne val quiz:Quiz, @ManyToOne val application:Application
)

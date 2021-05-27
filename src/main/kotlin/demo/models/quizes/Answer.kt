package demo.models.quizes

import javax.persistence.*

@Entity
@Table(name = "Answers")
data class Answer(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val text:String,val isCorrect:Boolean
)

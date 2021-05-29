package demo.models.quizes

import javax.persistence.*

@Entity
@Table(name = "answers")
data class Answer(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val text:String,val isCorrect:Boolean
)

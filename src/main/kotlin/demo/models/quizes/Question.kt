package demo.models.quizes

import javax.persistence.*

@Entity
@Table(name = "Questions")
data class Question(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val text:String,@Enumerated(EnumType.STRING) val type:QuestionType,
        @OneToMany val answers:Set<Answer>
)

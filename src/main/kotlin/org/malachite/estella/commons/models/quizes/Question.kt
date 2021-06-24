package org.malachite.estella.commons.models.quizes

import javax.persistence.*

@Entity
@Table(name = "questions")
data class Question(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val text:String,@Enumerated(EnumType.STRING) val type:QuestionType,
        @OneToMany @JoinColumn(name="question_id") val answers:Set<Answer>
)

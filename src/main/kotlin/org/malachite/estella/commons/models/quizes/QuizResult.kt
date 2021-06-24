package org.malachite.estella.commons.models.quizes

import org.malachite.estella.commons.models.offers.Application
import javax.persistence.*

@Entity
@Table(name = "quiz_results")
data class QuizResult(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @ManyToOne val quiz:Quiz, @ManyToOne val application: Application
)

package demo.models.offers

import javax.persistence.*

@Entity
@Table(name = "RecruitmentStages")
data class RecruitmentStage(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Enumerated(EnumType.STRING) val type: StageType
)

package org.malachite.estella.commons.models.offers

import javax.persistence.*

@Entity
@Table(name = "recruitment_stages")
data class RecruitmentStage(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    @Enumerated(EnumType.STRING) val type: StageType
)

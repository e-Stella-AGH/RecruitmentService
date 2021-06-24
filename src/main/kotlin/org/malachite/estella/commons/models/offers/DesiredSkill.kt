package org.malachite.estella.commons.models.offers

import javax.persistence.*

@Entity
@Table(name = "desired_skills")
data class DesiredSkill(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String,@Enumerated(EnumType.STRING) val level: SkillLevel
)

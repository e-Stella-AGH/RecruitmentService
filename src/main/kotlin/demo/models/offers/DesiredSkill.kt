package demo.models.offers

import javax.persistence.*

@Entity
@Table(name = "DesiredSkills")
data class DesiredSkill(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String,@Enumerated(EnumType.STRING) val level: SkillLevel
)

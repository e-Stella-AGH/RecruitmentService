package demo.models.offers

import demo.models.people.Hrpartner
import javax.persistence.*
import java.sql.Clob

@Entity
@Table(name = "Offers")
data class Offer(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String, @Lob val description:Clob, val postion:String,
        val minSalary:Long, val maxSalary:Long, val localization: String,
        @ManyToOne @JoinColumn(name = "hr_partner_id") val creator:Hrpartner,
        @ManyToMany val skills:Set<DesiredSkill>, @OneToOne val recruitmentProcess: RecruitmentProcess
)

package demo.models.offers

import demo.models.people.HrPartner
import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "offers")
data class Offer(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String, @Lob val description: Clob, val position:String,
        val minSalary:Long, val maxSalary:Long, val localization: String,
        @ManyToOne @JoinColumn(name = "hr_partners_id") val creator:HrPartner,
        @ManyToMany val skills:Set<DesiredSkill>, @OneToOne val recruitmentProcess: RecruitmentProcess?
)

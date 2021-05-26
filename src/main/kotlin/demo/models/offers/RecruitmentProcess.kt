package demo.models.offers

import java.sql.Date
import javax.persistence.*


@Entity
@Table(name = "RecruitmentProcesses")
data class RecruitmentProcess(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val startDate:Date, val endDate:Date, @OneToOne val offer: Offer,
        @OneToMany val stages:List<RecruitmentStage>
)

package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.people.HrPartner
import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "offers")
data class Offer(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val name:String, @Lob val description:Clob, val position:String,
        val minSalary:Long, val maxSalary:Long, val localization: String,
        @ManyToOne @JoinColumn(name = "hr_partners_id") val creator: HrPartner,
        @ManyToMany val skills:Set<DesiredSkill>,
        @OneToOne(mappedBy="offer", cascade = [CascadeType.REMOVE])
        @PrimaryKeyJoinColumn()
            val recruitmentProcess: RecruitmentProcess?
)

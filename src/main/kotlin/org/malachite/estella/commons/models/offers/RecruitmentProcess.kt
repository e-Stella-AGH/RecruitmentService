package org.malachite.estella.commons.models.offers

import org.malachite.estella.commons.models.quizes.Quiz
import org.malachite.estella.commons.models.tasks.Task
import java.sql.Date
import java.time.Instant
import javax.persistence.*


@Entity
@Table(name = "recruitment_processes")
data class RecruitmentProcess(
    @Id @Column(name = "offers_id") val id: Int?,
    val startDate: Date, val endDate: Date?,
    @OneToOne(cascade = [CascadeType.ALL]) @MapsId @JoinColumn(name = "offers_id") val offer: Offer,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER) @JoinColumn(name = "recruitment_processes_id") val stages: List<RecruitmentStage>
) {
    fun isStarted(): Boolean = startDate < Date.from(Instant.now())

}

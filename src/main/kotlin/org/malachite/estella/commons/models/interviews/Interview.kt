package org.malachite.estella.commons.models.interviews

import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.tasks.TaskStage
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "interviews")
data class Interview(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
    val dateTime: Timestamp, val minutesLength: Int,
    @OneToOne val applicationStage: ApplicationStageData
)

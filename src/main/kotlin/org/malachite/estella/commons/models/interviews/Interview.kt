package org.malachite.estella.commons.models.interviews

import org.malachite.estella.commons.models.offers.Application
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "interviews")
data class Interview(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
        val dateTime: Timestamp?, val minutesLength: Int,
        @ManyToOne val application: Application,
        @ElementCollection(fetch = FetchType.EAGER) val hosts: List<String>,
        @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name = "interviews_id") val notes: Set<InterviewNote>,
)


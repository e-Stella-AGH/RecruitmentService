package org.malachite.estella.commons.models.people

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "organizations")
data class Organization(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: UUID?,
        val name: String,
        @OneToOne(cascade = [CascadeType.ALL]) val user: User,
        val verified: Boolean = false
)

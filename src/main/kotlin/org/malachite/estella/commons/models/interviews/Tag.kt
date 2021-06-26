package org.malachite.estella.commons.models.interviews

import javax.persistence.*

@Entity
@Table(name = "tags")
data class Tag(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val text:String
)

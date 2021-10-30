package org.malachite.estella.commons.models.tasks

import javax.persistence.*

@Entity
@Table(name = "tasks")
data class Task(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val tests: Array<Byte>,
        @Lob val description: String,
        val descriptionFileName: String,
        val timeLimit: Int
)

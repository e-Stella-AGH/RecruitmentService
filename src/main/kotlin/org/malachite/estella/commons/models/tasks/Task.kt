package org.malachite.estella.commons.models.tasks

import java.sql.Blob
import java.sql.Clob
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "tasks")
data class Task(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val tests: Blob,
        @Lob val description: Clob,
        val descriptionFileName: String,
        val timeLimit: Int
)

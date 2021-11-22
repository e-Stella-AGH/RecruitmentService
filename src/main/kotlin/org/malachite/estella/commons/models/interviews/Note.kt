package org.malachite.estella.commons.models.interviews

import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "notes")
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
    val author: String, @ManyToMany val tags: Set<Tag>, @Lob val text: Clob?
)

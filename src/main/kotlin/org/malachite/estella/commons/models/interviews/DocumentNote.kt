package org.malachite.estella.commons.models.interviews

import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "document_notes")
data class DocumentNote(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val note: Clob,
        @ManyToMany @JoinTable(
                name = "document_notes_tags",
                joinColumns = [JoinColumn(name = "document_notes_id")],
                inverseJoinColumns = [JoinColumn(name = "tags_id")]
        ) val tags:Set<Tag>,
        @OneToMany @JoinColumn(name="document_notes_id") val marks:Set<DocumentMark>
)

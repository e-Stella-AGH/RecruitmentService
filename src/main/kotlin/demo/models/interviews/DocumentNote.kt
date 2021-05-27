package demo.models.interviews

import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "DocumentNotes")
data class DocumentNote(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val note: Clob,
        @ManyToMany @JoinTable(
                name = "DocumentNotesTags",
                joinColumns = [JoinColumn(name = "document_notes_id")],
                inverseJoinColumns = [JoinColumn(name = "tags_id")]
        ) val tags:Set<Tag>,
        @OneToMany val marks:Set<DocumentMark>
)

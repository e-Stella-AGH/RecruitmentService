package org.malachite.estella.commons.models.interviews

import org.malachite.estella.commons.models.people.JobSeekerFile
import javax.persistence.*

@Entity
@Table(name = "document_marks")
data class DocumentMark(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val line: Int, val width: Int, val color: String, @ManyToOne val document: JobSeekerFile
)
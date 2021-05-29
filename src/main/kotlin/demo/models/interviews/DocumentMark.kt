package demo.models.interviews

import demo.models.people.JobSeekerFile
import javax.persistence.*

@Entity
@Table(name = "document_marks")
data class DocumentMark(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val line: Int, val width: Int, val color: String, @ManyToOne val document: JobSeekerFile
)

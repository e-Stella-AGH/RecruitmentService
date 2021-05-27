package demo.models.interviews

import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "InterviewNotes")
data class InterviewNote(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val note:Clob
)

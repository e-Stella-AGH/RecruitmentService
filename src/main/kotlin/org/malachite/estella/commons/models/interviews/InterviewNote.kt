package org.malachite.estella.commons.models.interviews

import java.sql.Clob
import javax.persistence.*

@Entity
@Table(name = "interview_notes")
data class InterviewNote(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        @Lob val note:Clob
)

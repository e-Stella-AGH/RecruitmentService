package org.malachite.estella.commons.models.people

import org.springframework.transaction.annotation.Transactional
import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "job_seeker_files")
class JobSeekerFile(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val fileName:String,
        @Lob @Basic(fetch = FetchType.LAZY) val file:Blob
)
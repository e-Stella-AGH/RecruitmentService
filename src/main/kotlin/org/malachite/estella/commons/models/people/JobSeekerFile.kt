package org.malachite.estella.commons.models.people

import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "job_seeker_files")
class JobSeekerFile(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val file_name:String,
        @Lob val file:Blob
)
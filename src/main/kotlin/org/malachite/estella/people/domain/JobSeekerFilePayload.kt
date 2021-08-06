package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeekerFile
import java.util.*
import javax.sql.rowset.serial.SerialBlob

data class JobSeekerFilePayload(
    val id: Int?,
    val fileName: String,
    val fileBase64: String
) {
    fun toJobSeekerFile(): JobSeekerFile? = try {
        JobSeekerFile(
            id = id,
            fileName = fileName,
            file = SerialBlob(Base64.getDecoder().decode(fileBase64))
        )
    } catch (e: Exception) {
        println("Error during parsing JobSeekerFilePayload to JobSeekerFile")
        null
    }
}


data class JobSeekerFileDTO(
    val id:Int,
    val fileName: String,
    val fileBase64: String
)

fun JobSeekerFile.toJobSeekerFileDTO(): JobSeekerFileDTO = JobSeekerFileDTO(
    this.id!!,
    this.fileName,
    Base64.getEncoder().encodeToString(
        this.file.getBytes(1, this.file.length().toInt())
    )
)

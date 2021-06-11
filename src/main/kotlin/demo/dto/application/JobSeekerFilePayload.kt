package demo.dto.application

import demo.models.people.JobSeeker
import demo.models.people.JobSeekerFile
import java.util.*
import javax.sql.rowset.serial.SerialBlob

data class JobSeekerFilePayload(
        val fileName : String,
        val file_base64: String
){
    fun toJobSeekerFile(): JobSeekerFile? = try {
        JobSeekerFile(
            id = null,
            file_name = fileName,
            file = SerialBlob(Base64.getDecoder().decode(file_base64))
        )
    } catch (e: Exception) {
        null
    }
}

data class JobSeekerFileDTO(
        val fileName: String,
        val file_base64: String
){
    companion object {
        fun fromJobSeekerFile(jobSeekerFile: JobSeekerFile) = JobSeekerFileDTO(
                jobSeekerFile.file_name,
                Base64.getEncoder().encodeToString(
                        jobSeekerFile.file.getBytes(1, jobSeekerFile.file.length().toInt())
                )
        )
    }
}

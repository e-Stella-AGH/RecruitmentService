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

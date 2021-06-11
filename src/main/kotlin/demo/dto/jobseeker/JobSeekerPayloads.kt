package demo.dto.jobseeker

import demo.dto.user.UserDTO
import demo.models.people.JobSeeker
import demo.models.people.User
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

data class JobSeekerDTO(val user: UserDTO) {
    companion object {
        fun fromJobSeeker(jobSeeker: JobSeeker) = JobSeekerDTO(
                UserDTO.fromUser(jobSeeker.user)
        )
    }
}
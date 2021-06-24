package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker

data class JobSeekerDTO(val user: UserDTO) {
    companion object {
        fun fromJobSeeker(jobSeeker: JobSeeker) = JobSeekerDTO(
                UserDTO.fromUser(jobSeeker.user)
        )
    }
}
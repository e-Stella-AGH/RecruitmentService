package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker

data class JobSeekerDTO(val user: UserDTO)

fun JobSeeker.toJobSeekerDTO() =
    JobSeekerDTO(UserDTO.fromUser(this.user))

package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker

data class JobSeekerDTO(val id:Int?,val user: UserDTO)

fun JobSeeker.toJobSeekerDTO() =
    JobSeekerDTO(id,user.toUserDTO())

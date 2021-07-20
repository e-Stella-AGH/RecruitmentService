package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile

data class JobSeekerDTO(val id:Int?,val user: UserDTO, val files: List<JobSeekerFileDTO>)

fun JobSeeker.toJobSeekerDTO() =
    JobSeekerDTO(id,user.toUserDTO(),this.files.map { JobSeekerFileDTO.fromJobSeekerFile(it) })

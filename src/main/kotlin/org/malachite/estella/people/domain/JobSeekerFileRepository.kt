package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeekerFile

interface JobSeekerFileRepository {
    fun findById(id: Int):JobSeekerFile
    fun save(jobSeekerFile: JobSeekerFile): JobSeekerFile
    fun deleteById(id: Int)
}
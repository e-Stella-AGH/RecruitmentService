package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker
import java.util.*

interface JobSeekerRepository {
    fun findByUserId(user_id: Int): Optional<JobSeeker>
    fun save(updatedJobSeeker: JobSeeker): JobSeeker
    fun findAll(): MutableIterable<JobSeeker>
    fun deleteById(id: Int)
}
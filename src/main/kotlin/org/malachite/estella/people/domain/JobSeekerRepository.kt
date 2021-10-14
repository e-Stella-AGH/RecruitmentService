package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.JobSeeker
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface JobSeekerRepository {
    fun findByUserId(user_id: Int): Optional<JobSeeker>
    fun save(updatedJobSeeker: JobSeeker): JobSeeker
    fun findAll(): MutableIterable<JobSeeker>
    fun findByUserMail(mail:String):Optional<JobSeeker>
    fun deleteById(id: Int)
}
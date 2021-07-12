package org.malachite.estella.people

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.people.domain.JobSeekerRepository
import java.util.*

class DummyJobSeekerRepository: JobSeekerRepository {
    private val jobSeekers: MutableList<JobSeeker> = mutableListOf()

    override fun findByUserId(user_id: Int): Optional<JobSeeker> =
        jobSeekers.find { it.id == user_id }.let {
            Optional.ofNullable(it)
        }

    override fun save(updatedJobSeeker: JobSeeker): JobSeeker {
        jobSeekers.add(updatedJobSeeker)
        return updatedJobSeeker
    }

    override fun findAll(): MutableIterable<JobSeeker> {
        return jobSeekers
    }

    override fun deleteById(id: Int): Optional<JobSeeker> {
        val jobSeeker = findByUserId(id)
        jobSeekers.remove(jobSeeker.get())
        return jobSeeker
    }

    fun size() = jobSeekers.size
    fun clear() = jobSeekers.clear()
}
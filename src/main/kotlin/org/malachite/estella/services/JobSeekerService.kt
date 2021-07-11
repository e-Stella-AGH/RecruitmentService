package org.malachite.estella.services

import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.people.domain.JobSeekerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JobSeekerService(
    @Autowired private val jobSeekerRepository: JobSeekerRepository,
    @Autowired private val userService: UserService
) {
    fun getJobSeekers(): MutableIterable<JobSeeker> = jobSeekerRepository.findAll()

    fun getJobSeeker(id: Int): JobSeeker = jobSeekerRepository.findByUserId(id).get()

    fun registerJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        userService.registerUser(jobSeeker.user).let {
            jobSeeker.copy(user = it)
        }.let {
            jobSeekerRepository.save(it)
        }

    fun addJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        userService.addUser(jobSeeker.user).let {
            jobSeeker.copy(user = it)
        }.let {
            jobSeekerRepository.save(it)
        }

    fun deleteJobSeeker(id: Int) = jobSeekerRepository.deleteById(id)
}
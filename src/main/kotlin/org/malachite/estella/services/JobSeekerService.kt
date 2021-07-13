package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.people.domain.UserAlreadyExistsException
import org.malachite.estella.people.domain.UserNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class JobSeekerService(
    @Autowired private val jobSeekerRepository: JobSeekerRepository,
    @Autowired private val userService: UserService
): EStellaService() {

    override val throwable: Exception = UserNotFoundException()

    fun getJobSeekers(): MutableIterable<JobSeeker> = jobSeekerRepository.findAll()

    fun getJobSeeker(id: Int): JobSeeker = withExceptionThrower { jobSeekerRepository.findByUserId(id).get() } as JobSeeker

    fun registerJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        userService.registerUser(jobSeeker.user).let {
            jobSeeker.copy(user = it)
        }.let {
            jobSeekerRepository.save(it)
        }

    fun deleteJobSeeker(id: Int) = jobSeekerRepository.deleteById(id)
}
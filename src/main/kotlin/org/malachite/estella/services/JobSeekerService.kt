package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
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
    @Autowired private val mailService: MailService,
    @Autowired private val securityService: SecurityService
): EStellaService() {

    override val throwable: Exception = UserNotFoundException()

    fun getJobSeekers(): MutableIterable<JobSeeker> = jobSeekerRepository.findAll()

    fun getJobSeeker(id: Int): JobSeeker = withExceptionThrower { jobSeekerRepository.findByUserId(id).get() } as JobSeeker

    fun registerJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        try {
            jobSeekerRepository.save(jobSeeker).also {
                mailService.sendRegisterMail(it.user)
            }
        } catch(e: DataIntegrityViolationException) {
            throw UserAlreadyExistsException()
        }

    fun deleteJobSeeker(id: Int, jwt: String?) =
        if(!checkRights(id, jwt).contains(Permission.DELETE)) throw UnauthenticatedException()
        else deleteJobSeeker(id)

    private fun deleteJobSeeker(id: Int) = jobSeekerRepository.deleteById(id)

    private fun checkRights(id: Int, jwt: String?): Set<Permission> {
        if(securityService.isCorrectApiKey(jwt)) return Permission.allPermissions()
        securityService.getJobSeekerFromJWT(jwt)
            ?.let {
                if(id == it.id) return Permission.allPermissions()
                else throw UnauthenticatedException()
            }
            ?: throw UnauthenticatedException()
    }
}
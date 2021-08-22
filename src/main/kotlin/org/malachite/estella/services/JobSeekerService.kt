package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.JobSeekerFile
import org.malachite.estella.people.domain.JobSeekerFilePayload
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
    @Autowired private val jobSeekerFileService: JobSeekerFileService,
    @Autowired private val securityService: SecurityService
) : EStellaService<JobSeeker>() {

    override val throwable: Exception = UserNotFoundException()

    fun getJobSeekers(): MutableIterable<JobSeeker> = jobSeekerRepository.findAll()

    fun getJobSeeker(id: Int): JobSeeker = withExceptionThrower { jobSeekerRepository.findByUserId(id).get() }

    fun registerJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        createJobSeeker(jobSeeker)
            .also { mailService.sendRegisterMail(it.user) }

    fun createJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        try {
            jobSeekerRepository.save(jobSeeker)
        } catch (e: DataIntegrityViolationException) {
            throw UserAlreadyExistsException()
        }

    fun deleteJobSeeker(id: Int) {
        if (!checkRights(id).contains(Permission.DELETE)) throw UnauthenticatedException()
        jobSeekerRepository.deleteById(id)
    }

    private fun checkRights(id: Int): Set<Permission> {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return Permission.allPermissions()

        securityService.getJobSeekerFromContext()
            ?.let {
                if (id == it.id) return Permission.allPermissions()
                else null
            } ?: throw UnauthenticatedException()
    }

    fun getOrCreateJobSeeker(jobSeeker: JobSeeker): JobSeeker =
        jobSeekerRepository
            .findByUserMail(jobSeeker.user.mail).let { if (it.isPresent) it.get() else createJobSeeker(jobSeeker) }

    fun updateJobSeeker(updatedJobSeeker: JobSeeker) =
        jobSeekerRepository.save(updatedJobSeeker)


    fun addNewFiles(jobSeeker: JobSeeker, files: Set<JobSeekerFilePayload>): Set<JobSeekerFile> =
        jobSeeker.let {
            val applicationFiles = jobSeekerFileService.getOrAddFile(it, files)
            val newFiles = it.files.toSet() + applicationFiles
            updateJobSeeker(it.copy(user = it.user, files = newFiles.toMutableSet()))
            applicationFiles
        }.toSet()


    fun updateJobSeekerFiles(jobSeeker: JobSeeker, files: List<JobSeekerFilePayload>) {
        val previousFiles = jobSeeker.files
        val newFiles = jobSeekerFileService.updateFiles(previousFiles, files)
        updateJobSeeker(jobSeeker.copy(files = newFiles))

    }
}
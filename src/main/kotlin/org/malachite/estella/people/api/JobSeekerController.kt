package org.malachite.estella.people.api

import org.malachite.estella.commons.Message
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.*
import org.malachite.estella.services.JobSeekerFileService
import org.malachite.estella.services.JobSeekerService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.SQLNonTransientException

@RestController
@RequestMapping("/api/jobseekers")
class JobSeekerController(
    @Autowired private val jobSeekerService: JobSeekerService,
    @Autowired private val jobSeekerFileService: JobSeekerFileService,
    @Autowired private val securityService: SecurityService
) {

    @CrossOrigin
    @GetMapping
    fun getJobSeekers(): ResponseEntity<List<JobSeekerDTO>> =
        jobSeekerService.getJobSeekers().map { it.toJobSeekerDTO() }.let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/{jobseekerId}")
    fun getJobSeekerById(@PathVariable("jobseekerId") jobSeekerId: Int): ResponseEntity<JobSeekerDTO> =
        jobSeekerService.getJobSeeker(jobSeekerId)
            .let { ResponseEntity.ok(it.toJobSeekerDTO()) }

    @CrossOrigin
    @PostMapping
    fun registerJobSeeker(@RequestBody jobSeekerRequest: JobSeekerRequest): ResponseEntity<JobSeekerDTO> =
        try {
            jobSeekerRequest
                .toJobSeeker()
                .let { jobSeekerService.registerJobSeeker(it) }
                .let { OwnResponses.CREATED(it.toJobSeekerDTO()) }
        } catch (e: Exception) {
            when (e) {
                is DataIntegrityViolationException,
                is SQLNonTransientException ->
                    throw UserAlreadyExistsException()
                else -> {
                    println("Error msg: ${e.message}")
                    throw e
                }
            }
        }

    @CrossOrigin
    @DeleteMapping("/{jobSeekerId}")
    fun deleteJobSeeker(
        @PathVariable("jobSeekerId") jobSeekerId: Int
    ): ResponseEntity<Any> =
        jobSeekerService.deleteJobSeeker(jobSeekerId).let {
            ResponseEntity.ok(SuccessMessage)
        }

    @CrossOrigin
    @Transactional
    @GetMapping("/files")
    fun getJobSeekerFiles(): ResponseEntity<List<JobSeekerFileDTO>> =
        securityService.getJobSeekerFromContextUnsafe().files
            .map { it.toJobSeekerFileDTO() }
            .let { ResponseEntity.ok(it) }


    @CrossOrigin
    @PutMapping("/files")
    fun addJobSeekerFiles(
        @RequestBody jobSeekerFileRequest: JobSeekerFilesRequest
    ): ResponseEntity<Message> =
        securityService.getJobSeekerFromContext()!!
            .let { jobSeekerService.updateJobSeekerFiles(it, jobSeekerFileRequest.files) }
            .let { OwnResponses.SUCCESS }

}

data class JobSeekerRequest(
    val mail: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class JobSeekerFilesRequest(
    val files: List<JobSeekerFilePayload>
)

fun JobSeekerRequest.toJobSeeker() = JobSeeker(
    null,
    User(null, firstName, lastName, mail, password),
    mutableSetOf()
)


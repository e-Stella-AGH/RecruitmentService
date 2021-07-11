package org.malachite.estella.people.api

import org.malachite.estella.commons.SuccessMessage
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.JobSeekerService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/jobseekers")
class JobSeekerController(
    @Autowired private val jobSeekerService: JobSeekerService
) {

    @CrossOrigin
    @GetMapping
    fun getJobSeekers(): ResponseEntity<MutableIterable<JobSeeker>> =
        jobSeekerService.getJobSeekers().let {
            ResponseEntity(it, HttpStatus.OK)
        }

    @CrossOrigin
    @GetMapping("/{jobseekerId}")
    fun getJobSeekerById(@PathVariable("jobseekerId") jobSeekerId: Int): ResponseEntity<JobSeeker> =
        jobSeekerService.getJobSeeker(jobSeekerId).let {
            ResponseEntity(it, HttpStatus.OK)
        }

    @CrossOrigin
    @PostMapping()
    fun addJobSeeker(@RequestBody jobSeekerRequest: JobSeekerRequest) =
        jobSeekerRequest.toJobSeeker().let {
            jobSeekerService.registerJobSeeker(it)
        }.let {
            ResponseEntity(SuccessMessage, HttpStatus.CREATED)
        }

    @CrossOrigin
    @DeleteMapping("/{jobSeekerId}")
    fun deleteJobSeeker(@PathVariable("jobSeekerId") jobSeekerId: Int) =
        jobSeekerService.deleteJobSeeker(jobSeekerId).let {
            ResponseEntity(SuccessMessage, HttpStatus.OK)
        }

    //TODO - add endpoint to insert files?
}

data class JobSeekerRequest(
    val mail: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
fun JobSeekerRequest.toJobSeeker() = JobSeeker(
    null,
    User(null, firstName, lastName, mail, password),
    setOf()
)
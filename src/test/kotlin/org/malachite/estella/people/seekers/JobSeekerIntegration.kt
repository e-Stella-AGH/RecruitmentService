package org.malachite.estella.people.seekers

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.JobSeekerFileDTO
import org.malachite.estella.util.DatabaseReset
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class JobSeekerIntegration: BaseIntegration() {


    @Test
    @Order(1)
    fun `should add job seeker`() {
        val response = registerJobSeeker()
        expectThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        checkOnJobSeekersAndUsers()
    }

    @Test
    @Order(2)
    fun `should throw exception, when job seeker already exists`() {
        val response = registerJobSeeker()
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        checkOnJobSeekersAndUsers()
    }

    @Test
    @Order(3)
    fun `should update jobSeekerFiles`() {
        val response = httpRequest(
            path = "/api/jobseekers/files",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            body = mapOf("files" to listOf(
                getJobSeekerFilePayload("file1"),
                getJobSeekerFilePayload("file2")
            ))
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val files = getJobSeekerFiles()
        expectThat(files.size).isEqualTo(2)
        expectThat(files.map { it.fileName }.sorted()).isEqualTo(listOf("file1","file2"))
    }

    @Test
    @Order(4)
    fun `should update remove file2 and add file3`() {
        val oldFile = getJobSeekerFiles().first()
        val response = httpRequest(
            path = "/api/jobseekers/files",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken()),
            body = mapOf("files" to listOf(
                getJobSeekerFilePayload(oldFile.fileName,oldFile.id),
                getJobSeekerFilePayload("file3")
            ))
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val files = getJobSeekerFiles()
        expectThat(files.size).isEqualTo(2)
        expectThat(files.map { it.fileName }.sorted()).isEqualTo(listOf(oldFile.fileName,"file3"))
        expectThat(files.minOf { it.id }).isEqualTo(oldFile.id)
    }




    @Test
    @Order(5)
    fun `should delete job seeker`() {
        val jobSeeker = getJobSeekers().firstOrNull { it.user.mail == jobseekerMail }
        expectThat(jobSeeker).isNotNull()
        val response = httpRequest(
            path = "/api/jobseekers/${jobSeeker?.id}",
            method = HttpMethod.DELETE,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken())
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val deletedJobSeeker = getJobSeekers().firstOrNull { it.user.mail == jobseekerMail }
        expectThat(deletedJobSeeker).isNull()
    }

    private fun checkOnJobSeekersAndUsers() {
        val users = getUsers().filter { it.mail == jobseekerMail }
        val jobSeekers = getJobSeekers().filter { it.user.mail == jobseekerMail }

        expectThat(users.size).isEqualTo(1)
        expectThat(jobSeekers.size).isEqualTo(1)
    }

    private fun registerJobSeeker(): Response {
        return httpRequest(
            path = "/api/jobseekers/",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to jobseekerMail,
                "password" to password,
                "firstName" to name,
                "lastName" to surname
            )
        )
    }

    private fun getUsers(): List<User> {
        val response = httpRequest(
            path = "/api/users",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as List<Map<String, Any>>
            return it.map { it.toUser() }
        }
    }

    private fun getJobSeekers(): List<JobSeeker> {
        val response = httpRequest(
            path = "/api/jobseekers",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body.let {
            it as List<Map<String, Any>>
            return it.map { it.toJobSeeker() }
        }
    }

    private fun loginUser(userMail: String = jobseekerMail, userPassword: String = password): Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getJobSeekerFiles():List<JobSeekerFileDTO> {
        val response = httpRequest(
            path="/api/jobseekers/files",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken())
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return response.body.let { body ->
            body as List<Map<String, Any>>
            body.map {
                it.toJobSeekerFileDto()
            }
        }
    }


    private fun getAuthToken() =
        getAuthToken(jobseekerMail,password)

    private val name = "name"
    private val surname = "surname"
    private val jobseekerMail = "examplemail@jobseeker.pl"
    private val password = "123"
}
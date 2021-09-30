package org.malachite.estella.tasks

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.util.DatabaseReset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.map
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TasksIntegration: BaseIntegration() {

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository
    @Autowired
    private lateinit var tasksRepository: TaskRepository
    @Autowired
    private lateinit var securityService: SecurityService

    @Test
    @Order(1)
    fun `should be able to post task to organization`() {
        val organization = organizationRepository.findAll().first()
        val password = securityService.hashOrganization(organization)
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.POST,
            mapOf(EStellaHeaders.passwordHeader to password, "Content-Type" to "application/json"),
            body = mapOf(
                "testsBase64" to encodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to encodedFile,
                "timeLimit" to timeLimit,
                "deadline" to deadline
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(tasksRepository.findAll()).isNotEmpty()
    }

    @Test
    @Order(2)
    fun `should respond with unauth when bad password post task`() {
        val organization = organizationRepository.findAll().first()
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.POST,
            mapOf(EStellaHeaders.passwordHeader to "abcdfeg", "Content-Type" to "application/json"),
            body = mapOf(
                "testsBase64" to encodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to encodedFile,
                "timeLimit" to timeLimit,
                "deadline" to deadline
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(3)
    fun `should be able to get task from organization`() {
        val organization = organizationRepository.findAll().first()
        val password = securityService.hashOrganization(organization)
        val response = httpRequest(
            path = "/api/tasks?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.passwordHeader to password)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expect {
            val tasksDto = (response.body as List<Map<String, Any>>).toTaskDto()
            that(tasksDto.size).isEqualTo(2)
            that(tasksDto)
                .map { it.toAssertionTaskDto() }
                .any {
                    isEqualTo(AssertionTaskDto(encodedFile, descriptionFileName, encodedFile, timeLimit))
                }
        }
    }

    @Test
    @Order(4)
    fun `should send unauthorized for bad password get tasks`() {
        val organization = organizationRepository.findAll().first()
        val response = httpRequest(
            path = "/api/tasks?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.passwordHeader to "abcdefdf")
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    private val testsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests.json"))
    private val encodedFile = Base64.getEncoder().encodeToString(testsFile)

    private val descriptionFileName = "description.pdf"
    private val timeLimit = 30
    private val deadline = Timestamp.from(Instant.now())

    private fun TaskDto.toAssertionTaskDto() = AssertionTaskDto(
        testsBase64,
        descriptionFileName,
        descriptionBase64,
        timeLimit
    )
    private data class AssertionTaskDto(
        val testsBase64: String,
        val descriptionFileName: String,
        val descriptionBase64: String,
        val timeLimit: Int
    )
}
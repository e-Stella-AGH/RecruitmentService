package org.malachite.estella.tasks

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.process.domain.RecruitmentProcessRepository
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.util.DatabaseReset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TasksIntegration: BaseIntegration() {

    @Autowired
    private lateinit var processRepository: RecruitmentProcessRepository
    @Autowired
    private lateinit var tasksRepository: TaskRepository

    @Test
    @Order(1)
    fun `should be able to post task to recruitment process`() {
        val process = processRepository.findAll().first()
        val response = httpRequest(
            "/api/tasks/${process.id}",
            method = HttpMethod.POST,
            mapOf(EStellaHeaders.jwtToken to "Admin", "Content-Type" to "application/json"),
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
    fun `should be able to get task from recruitment process`() {
        val process = processRepository.findAll().first()
        val response = httpRequest(
            path = "/api/tasks?process=${process.id}",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expect {
            println(response.body)
            val taskDto = (response.body as List<Map<String, Any>>).toTaskDto()
            that(taskDto.size).isEqualTo(1)
            val task = taskDto[0]
            that(task.descriptionBase64).isEqualTo(encodedFile)
            that(task.descriptionFileName).isEqualTo(descriptionFileName)
            that(task.testsBase64).isEqualTo(encodedFile)
            that(task.timeLimit).isEqualTo(timeLimit)
        }
    }

    private val testsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests.json"))
    private val encodedFile = Base64.getEncoder().encodeToString(testsFile)

    private val descriptionFileName = "description.pdf"
    private val timeLimit = 30
    private val deadline = Timestamp.from(Instant.now())

}
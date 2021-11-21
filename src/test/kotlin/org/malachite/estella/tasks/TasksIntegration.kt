package org.malachite.estella.tasks

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.toBase64String
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.TaskTestCaseDto
import org.malachite.estella.process.domain.encodeToJson
import org.malachite.estella.process.domain.toTaskDto
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.hrPartners
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.*
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TasksIntegration : BaseIntegration() {


    private var applicationId = 0


    fun onStart(){
        startOffer(getOffer())
        applicationId =
            (applyForOffer(getJobSeeker(), password, getOffer()).body as Map<String, Any>).toApplicationDTO().id!!
        updateStage(applicationId, hrPartner.user.mail, password)
        updateStage(applicationId, hrPartner.user.mail, password)
    }

    fun getApplication() =
        applicationRepository.findAll()[0]

    @BeforeEach
    fun setup() {
        EmailServiceStub.stubForSendEmail()
    }

    @Test
    @Transactional
    @Order(1)
    fun `should be able to post task to organization`() {
        onStart()
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.POST,
            mapOf(EStellaHeaders.devPassword to password, "Content-Type" to "application/json"),
            body = mapOf(
                "testsBase64" to anotherEncodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to encodedFile,
                "timeLimit" to timeLimit
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
            mapOf(EStellaHeaders.devPassword to wrongDevPassword, "Content-Type" to "application/json"),
            body = mapOf(
                "testsBase64" to encodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to encodedFile,
                "timeLimit" to timeLimit
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(3)
    fun `should respond with 400 without parameter`() {
        val response = httpRequest(
            "/api/tasks",
            method = HttpMethod.POST,
            body = mapOf(
                "testsBase64" to encodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to encodedFile,
                "timeLimit" to timeLimit,
                "deadline" to deadline
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }



    @Test
    @Order(4)
    fun `should update tests with object`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val oldTask = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${oldTask.id!!}/tests/object?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("tests" to testObjects),
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedTask = taskRepository.findById(oldTask.id!!).get()
        expectThat(updatedTask.tests.toBase64String()).isEqualTo(Base64.getEncoder().encode(testObjects.encodeToJson().toByteArray()).decodeToString())
    }

    @Test
    @Order(5)
    fun `should unauth on update tests with object`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val oldTask = organization.tasks.maxByOrNull { it.id!! }!!
        val response1 = httpRequest(
            "/api/tasks/${oldTask.id!!}/tests/object",
            method = HttpMethod.PUT,
            body = mapOf("tests" to testObjects),
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response1.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val response2 = httpRequest(
            "/api/tasks/${oldTask.id!!}/tests/object?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("tests" to testObjects),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
        expectThat(response2.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val response3 = httpRequest(
            "/api/tasks/abc/tests/object?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("tests" to testObjects),
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response3.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(6)
    fun `should bad request on update tests with object`() {
        val organization = organizationRepository.findAll().first()
        val oldTask = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${oldTask.id!!}/tests/object?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("tests" to testObjects),
            headers = mapOf(
                EStellaHeaders.devPassword to wrongDevPassword,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(7)
    fun `should update tests with file`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val oldTask = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${oldTask.id!!}/tests/file?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("testsBase64" to encodedFile),
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedTask = taskRepository.findById(oldTask.id!!).get()
        expectThat(updatedTask.tests.toBase64String()).isEqualTo(encodedFile)
    }


    @Test
    @Order(8)
    fun `should unath on update tests with file`() {
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${task.id!!}/tests/file?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("testsBase64" to encodedFile),
            headers = mapOf(
                EStellaHeaders.devPassword to wrongDevPassword,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Transactional
    @Order(9)
    fun `should bad request on update tests with file`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)

        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response1 = httpRequest(
            "/api/tasks/${task.id!!}/tests/file?owner=${organization.id!!}",
            method = HttpMethod.PUT,
            body = mapOf("testsBase64" to encodedFile),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
        expectThat(response1.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val response2 = httpRequest(
            "/api/tasks/${task.id!!}/tests/file",
            method = HttpMethod.PUT,
            body = mapOf("testsBase64" to encodedFile),
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response2.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(10)
    fun `should be able to get task from organization`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.last()
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)

        val response = httpRequest(
            path = "/api/tasks?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to password)
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
    @Order(11)
    fun `should be able to get task tests`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.last()
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val task = organization.tasks.maxByOrNull { it.id!! }!!

        val response = httpRequest(
            path = "/api/tasks/${task.id}/tests?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to password)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(response.body).isEqualTo(encodedFile)
    }

    @Test
    @Order(12)
    fun `should unauth on get task tests`() {
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!

        val response = httpRequest(
            path = "/api/tasks/${task.id}/tests?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to wrongDevPassword)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }


    @Test
    @Order(13)
    fun `should bad request on get task tests`() {
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!

        val response1 = httpRequest(
            path = "/api/tasks/${task.id}/tests?owner=${organization.id}",
            method = HttpMethod.GET
        )
        expectThat(response1.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val response2 = httpRequest(
            path = "/api/tasks/${task.id}/tests",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to wrongDevPassword)
        )
        expectThat(response2.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(14)
    fun `should send unauthorized for bad password get tasks`() {
        val organization = organizationRepository.findAll().first()
        val response = httpRequest(
            path = "/api/tasks?owner=${organization.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to wrongDevPassword)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(15)
    fun `should send 400`() {
        val response = httpRequest(
            path = "/api/tasks",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to wrongDevPassword)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(16)
    fun `should be able to update task`() {
        onStart()
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.PUT,
            mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            ),
            body = mapOf(
                "id" to task.id!!,
                "testsBase64" to anotherEncodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to anotherEncodedFile,
                "timeLimit" to updatedTimeLimit
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedTask = tasksRepository.findById(task.id!!).get()
        expectThat(updatedTask.toTaskDto().toAssertionTaskDto()).isEqualTo(
            AssertionTaskDto(anotherEncodedFile, descriptionFileName, anotherEncodedFile, updatedTimeLimit)
        )
    }

    @Test
    @Order(17)
    fun `should unauth on update task`() {
        onStart()
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.PUT,
            mapOf(
                EStellaHeaders.devPassword to wrongDevPassword,
                "Content-Type" to "application/json"
            ),
            body = mapOf(
                "id" to task.id!!,
                "testsBase64" to anotherEncodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to anotherEncodedFile,
                "timeLimit" to updatedTimeLimit
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(18)
    fun `should bad request on update task`() {
        onStart()
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response1 = httpRequest(
            "/api/tasks",
            method = HttpMethod.PUT,
            mapOf(
                EStellaHeaders.devPassword to wrongDevPassword,
                "Content-Type" to "application/json"
            ),
            body = mapOf(
                "id" to task.id!!,
                "testsBase64" to anotherEncodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to anotherEncodedFile,
                "timeLimit" to updatedTimeLimit
            )
        )
        expectThat(response1.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val response2 = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.PUT,
            mapOf(
                "Content-Type" to "application/json"
            ),
            body = mapOf(
                "id" to task.id!!,
                "testsBase64" to anotherEncodedFile,
                "descriptionFileName" to descriptionFileName,
                "descriptionBase64" to anotherEncodedFile,
                "timeLimit" to updatedTimeLimit
            )
        )
        expectThat(response2.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(19)
    fun `should unauth on delete task`() {
        val organization = organizationRepository.findAll().first()
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${task.id}?owner=${organization.id}",
            method = HttpMethod.DELETE,
            headers = mapOf(
                EStellaHeaders.devPassword to wrongDevPassword,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @Order(20)
    fun `should be able to delete task`() {
        val organization = organizationRepository.findAll().first()
        val applicationStage = getApplication().applicationStages.maxByOrNull { it.id!! }!!
        val password = securityService.hashOrganization(organization, applicationStage.tasksStage!!)
        val task = organization.tasks.maxByOrNull { it.id!! }!!
        val response = httpRequest(
            "/api/tasks/${task.id}?owner=${organization.id}",
            method = HttpMethod.DELETE,
            headers = mapOf(
                EStellaHeaders.devPassword to password,
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedOrg = organizationRepository.findById(organization.id!!)
        expectThat(updatedOrg.get().tasks.find { it.id == task.id }).isNull()
    }

    @Test
    @Order(21)
    fun `should bad request on malformed dev password`() {
        val organization = organizationRepository.findAll().first()
        val response = httpRequest(
            "/api/tasks?owner=${organization.id}",
            method = HttpMethod.POST,
            headers = mapOf(
                EStellaHeaders.devPassword to "haha",
                "Content-Type" to "application/json"
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    private fun startOffer(offer: Offer) {
        httpRequest(
            path = "/api/process/${offer.id}/start",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, "a"))
        )
    }

    private val wrongDevPassword = Base64.getEncoder().encode("${UUID.randomUUID()}:${UUID.randomUUID()}".toByteArray()).decodeToString()

    private val testsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests.json"))
    private val encodedFile = Base64.getEncoder().encodeToString(testsFile)

    private val anotherTestsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests2.json"))
    private val anotherEncodedFile = Base64.getEncoder().encodeToString(anotherTestsFile)

    private val testObjects = listOf(
        TaskTestCaseDto(null, "xd", "xd"),
        TaskTestCaseDto(null, "xdd", "xdd"),
    )

    private val descriptionFileName = "description.pdf"
    private val timeLimit = 30
    private val deadline = Timestamp.from(Instant.now())

    private val updatedTimeLimit = 60

    private fun TaskDto.toAssertionTaskDto() = AssertionTaskDto(
        testsBase64,
        descriptionFileName,
        descriptionBase64,
        timeLimit
    )

    private fun getJobSeeker() = jobSeekerRepository.findAll().first()

    private val hrPartner = hrPartners[1]

    private val password = "a"

    private fun getOffer(which: Int = 0) =
        offerRepository.findAll().filter { it.creator.user.mail == hrPartner.user.mail }[which]


    private data class AssertionTaskDto(
        val testsBase64: String,
        val descriptionFileName: String,
        val descriptionBase64: String,
        val timeLimit: Int
    )
}
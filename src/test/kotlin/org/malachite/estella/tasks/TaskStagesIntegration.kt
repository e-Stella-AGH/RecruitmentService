package org.malachite.estella.tasks

import org.junit.BeforeClass
import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.BaseIntegration.Companion.httpRequest
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.loader.FakeOrganizations
import org.malachite.estella.commons.loader.FakeUsers
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.hrPartners
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.event.annotation.BeforeTestClass
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TaskStagesIntegration : BaseIntegration() {

    private var applicationId = 0

    fun onStart(){
        startOffer(getOffer())
        applicationId =
                (applyForOffer(getJobSeeker(), password, getOffer()).body as Map<String, Any>).toApplicationDTO().id!!
        updateStage(applicationId, hrPartner.user.mail, password)
        updateStage(applicationId, hrPartner.user.mail, password, setOf("dev1@a.com", "dev2@a.com"))
        addTask(TaskDto(null, encodedFile, descriptionFileName, encodedFile, timeLimit))
        addTask(TaskDto(null, encodedFile, descriptionFileName, encodedFile, timeLimit))
        addTask(TaskDto(null, encodedFile, descriptionFileName, encodedFile, timeLimit))
    }

    @BeforeEach
    fun setup() {
        EmailServiceStub.stubForSendEmail()
    }

    @Test
    @Order(1)
    fun `should be able to add tasks to taskStage`() {
//        onStart()
//        val tasks = getOrganization().tasks
//        val password = securityService.hashOrganization(getOrganization(), getTaskStage())
//        val response = httpRequest(
//                "/api/taskStages?taskStage=${getTaskStage().id}",
//                method = HttpMethod.PUT,
//                headers = mapOf(EStellaHeaders.devPassword to password),
//                body = mapOf("tasks" to tasks)
//        )
//
//        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

    }

    fun addTask(task: TaskDto) {
        val password = securityService.hashOrganization(getOrganization(), getTaskStage())
        val response = httpRequest(
                "/api/tasks?owner=${getOrganization().id}",
                method = HttpMethod.POST,
                mapOf(EStellaHeaders.devPassword to password),
                body = mapOf(
                        "testsBase64" to task.testsBase64,
                        "descriptionFileName" to task.descriptionFileName,
                        "descriptionBase64" to task.descriptionBase64,
                        "timeLimit" to task.timeLimit
                )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun startOffer(offer: Offer) {
        httpRequest(
                path = "/api/process/${offer.id}/start",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, "a"))
        )
    }

    private fun getOffer(which: Int = 0) =
            offerRepository.findAll().filter { it.creator.user.mail == hrPartner.user.mail }.get(which)

    private fun getJobSeeker() = jobSeekerRepository.findAll().first()

    fun getApplication() =
            applicationRepository.findById(applicationId).get()

    fun getOrganization() =
            organizationRepository.findAll().first()

    fun getTaskStage() =
            getApplication().applicationStages.sortedBy { it.id }.last().tasksStage!!


    private val hrPartner = hrPartners[1]

    private val password = "a"

    private val organization = FakeOrganizations.getCompanies(FakeUsers.organizationUsers)[0]
    private val testsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests.json"))
    private val encodedFile = Base64.getEncoder().encodeToString(testsFile)

    private val descriptionFileName = "description.pdf"
    private val timeLimit = 30


}
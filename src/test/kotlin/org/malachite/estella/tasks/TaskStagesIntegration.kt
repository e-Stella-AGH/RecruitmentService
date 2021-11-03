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
import org.malachite.estella.process.domain.toTaskDto
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
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
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
        onStart()
        val tasks = getOrganization().tasks
        var taskStage = getTaskStage()
        expectThat(taskStage.tasksResult.size).isEqualTo(0)
        val password = securityService.hashOrganization(getOrganization(), taskStage)
        val response = httpRequest(
                "/api/taskStages?taskStage=${taskStage.id}",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.devPassword to password),
                body = mapOf("tasks" to listOf(tasks.first().id))
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expect {
            val stagesTasks = taskStageRepository.findById(taskStage.id!!).get().tasksResult.map { it.task }
            that(stagesTasks.size).isEqualTo(1)
            that(stagesTasks.map { it.id }).containsExactlyInAnyOrder(listOf(tasks.first().id))
        }
    }

    @Test
    @Order(2)
    fun `should be able to update tasks in taskStage by InterviewUuid`() {
        applicationId = applicationRepository.getAllByJobSeekerId(getJobSeeker().id!!).first().id!!
        val tasks = getOrganization().tasks
        var taskStage = getTaskStage()
        val interview = interviewRepository.findAll().first { it.applicationStage.id == taskStage.applicationStage.id }
        val password = securityService.hashOrganization(getOrganization(), taskStage)
        val response = httpRequest(
                "/api/taskStages?interview=${interview.id}",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.devPassword to password),
                body = mapOf("tasks" to listOf(tasks.first(), tasks.last()).map { it.id })
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(taskStage.tasksResult.size).isEqualTo(1)
        expect {
            val stagesTasks = taskStageRepository.findById(taskStage.id!!).get().tasksResult.map { it.task }
            that(stagesTasks.size).isEqualTo(2)
            that(stagesTasks.map { it.id }).containsExactlyInAnyOrder(listOf(tasks.first(), tasks.last()).map { it.id })
        }
    }

    @Test
    @Order(3)
    fun `should return unauth when trying to set task with bad password`() {
        applicationId = applicationRepository.getAllByJobSeekerId(getJobSeeker().id!!).first().id!!
        val tasks = getOrganization().tasks
        var taskStage = getTaskStage()
        val interview = interviewRepository.findAll().first { it.applicationStage.id == taskStage.applicationStage.id }
        val password = "xd"
        val response = httpRequest(
                "/api/taskStages?interview=${interview.id}",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.devPassword to password),
                body = mapOf("tasks" to listOf(tasks.first().id))
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        expectThat(taskStage.tasksResult.size).isEqualTo(2)
    }

    @Test
    @Order(4)
    fun `should return all tasks by taskstage UUID`() {
        applicationId = applicationRepository.getAllByJobSeekerId(getJobSeeker().id!!).first().id!!
        val tasks = getOrganization().tasks
        var taskStage = getTaskStage()
        val password = securityService.hashOrganization(getOrganization(), taskStage)
        val response = httpRequest(
                "/api/tasks?taskStage=${taskStage.id}",
                method = HttpMethod.GET,
                headers = mapOf(EStellaHeaders.devPassword to password)
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as List<Map<String, Any>>
        expect {
            that(response.body.map { it.toTaskDto() }).containsExactlyInAnyOrder(taskStage.tasksResult[0].task.toTaskDto(), taskStage.tasksResult[1].task.toTaskDto())
        }
    }

    @Test
    @Order(5)
    fun `should return all tasks by devMail`() {
        applicationId = applicationRepository.getAllByJobSeekerId(getJobSeeker().id!!).first().id!!
        var taskStage = getTaskStage()
        val password = securityService.hashOrganization(getOrganization(), taskStage)
        val devMail = String(Base64.getEncoder().encode("dev1@a.com".toByteArray()))
        val response = httpRequest(
                "/api/tasks?devMail=$devMail&owner=${getOrganization().id}",
                method = HttpMethod.GET,
                headers = mapOf(EStellaHeaders.devPassword to password)
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as List<Map<String, Any>>
        expect {
            that(response.body.map { it.toTaskDto() }).containsExactlyInAnyOrder(taskStage.tasksResult[0].task.toTaskDto(), taskStage.tasksResult[1].task.toTaskDto())
        }
    }

    @Test
    @Order(6)
    fun `should return bad request when trying to set tasks to old task stage`() {
        applicationId = applicationRepository.getAllByJobSeekerId(getJobSeeker().id!!).first().id!!
        updateStage(applicationId, hrPartner.user.mail, password)
        val tasks = getOrganization().tasks
        val taskStage = getPreviousTaskStage()
        expectThat(taskStage.tasksResult.size).isEqualTo(2)
        val password = securityService.hashOrganization(getOrganization(), taskStage)
        val response = httpRequest(
                "/api/taskStages?taskStage=${taskStage.id}",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.devPassword to password),
                body = mapOf("tasks" to listOf(tasks.first().id))
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
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
            offerRepository.findAll().filter { it.creator.user.mail == hrPartner.user.mail }[which]

    private fun getJobSeeker() = jobSeekerRepository.findAll().first()

    fun getApplication() =
            applicationRepository.findById(applicationId).get()

    fun getOrganization() =
            recruitmentProcessService.getProcessFromStage(getTaskStage().applicationStage).offer.creator.organization

    fun getTaskStage() =
            getApplication().applicationStages.maxByOrNull { it.id!! }!!.tasksStage!!

    fun getPreviousTaskStage() =
            getApplication().applicationStages.sortedBy { -it.id!! }[1].tasksStage!!


    private val hrPartner = hrPartners[1]

    private val password = "a"

    private val testsFile = Files.readAllBytes(Paths.get("src/test/kotlin/org/malachite/estella/tasks/tests.json"))
    private val encodedFile = Base64.getEncoder().encodeToString(testsFile)

    private val descriptionFileName = "description.pdf"
    private val timeLimit = 30


}
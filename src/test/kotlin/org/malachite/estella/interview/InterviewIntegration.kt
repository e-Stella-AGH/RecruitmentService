package org.malachite.estella.interview

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.interview.domain.InterviewRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class InterviewIntegration : BaseIntegration() {

    @Autowired
    private lateinit var interviewRepository: InterviewRepository

    @Autowired
    private lateinit var jobSeekerRepository: JobSeekerRepository

    @Autowired
    private lateinit var recruitmentStageRepository: RecruitmentStageRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var applicationStageDataRepository: ApplicationStageRepository

    @Autowired
    private lateinit var taskStageRepository: TaskStageRepository

    @Autowired
    private lateinit var securityService: SecurityService

    @Autowired
    private lateinit var recruitmentProcessService: RecruitmentProcessService

    private lateinit var application: Application
    private lateinit var applicationStageData: ApplicationStageData
    private lateinit var organization: Organization
    private lateinit var jobseeker: JobSeeker
    private lateinit var hrPartner: HrPartner

    @BeforeEach
    fun prepareApplication() {
        val jobseeker = jobSeekerRepository.findAll().first()

        val stage = recruitmentStageRepository.findAll().first()
        hrPartner = recruitmentProcessService.getProcesses().first { it.stages.contains(stage) }.offer.creator
        val application = Application(
            null,
            Date(Calendar.getInstance().time.time),
            ApplicationStatus.IN_PROGRESS,
            jobseeker,
            mutableSetOf(),
            mutableListOf()
        )
        val savedApplication = applicationRepository.save(application)
        val applicationStageData = ApplicationStageData(null, stage, savedApplication, null, null, setOf())
        var savedApplicationStageData = applicationStageDataRepository.save(applicationStageData)
        var taskStage = TaskStage(null, listOf(), savedApplicationStageData)
        taskStage = taskStageRepository.save(taskStage)
        savedApplicationStageData =
            applicationStageDataRepository.save(savedApplicationStageData.copy(tasksStage = taskStage))
        this.application = savedApplication
        this.applicationStageData = applicationStageDataRepository.findById(savedApplicationStageData.id!!).get()
        this.organization = recruitmentProcessService.getProcessFromStage(stage.id!!).offer.creator.organization
        this.jobseeker = jobseeker
    }

    @AfterEach
    fun clearApplication() {
        interviewRepository.findAll().forEach { interviewRepository.deleteById(it.id!!) }
    }

    @Test
    @Order(1)
    fun `should return jobseeker name`() {
        val interview = Interview(null, null, null, applicationStageData, setOf())
        interviewRepository.save(interview)
        val interviewId = interviewRepository.findAll().first().id
        val response = httpRequest(
            "/api/interview/jobseeker/${interviewId}",
            method = HttpMethod.GET
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val responseBody = (response.body as Map<String, Any>).toJobSeekerNameDTO()
        expectThat(responseBody.firstName).isEqualTo(jobseeker.user.firstName)
        expectThat(responseBody.lastName).isEqualTo(jobseeker.user.lastName)
    }

    @Test
    @Order(2)
    fun `should return bad request`() {
        val INVALID_UUID = "xd"
        val response = httpRequest(
            "/api/interview/jobseeker/${INVALID_UUID}",
            method = HttpMethod.GET
        )
        withStatusAndMessage(response, "Invalid UUID", HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(3)
    fun `should return not found`() {
        val NOT_EXISTS = UUID.randomUUID()
        val response = httpRequest(
            "/api/interview/jobseeker/${NOT_EXISTS}",
            method = HttpMethod.GET
        )
        withStatusAndMessage(response, "We couldn't find this interview", HttpStatus.NOT_FOUND)
    }

    @Test
    @Order(4)
    fun `should return interview with later date when new date is set`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interviewRepository.save(interview)
        interview = Interview(null, Timestamp.valueOf(LocalDateTime.now()), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        val response = httpRequest(
            "/api/interview/newest/${application.id}",
            method = HttpMethod.GET
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as Map<String, String>
        expectThat(response.body["uuid"]).isEqualTo(interview.id.toString())
    }

    @Test
    @Order(5)
    fun `should return interview with later date when new date is null`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interviewRepository.save(interview)
        interview = Interview(null, null, null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        val response = httpRequest(
            "/api/interview/newest/${application.id}",
            method = HttpMethod.GET
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as Map<String, Any>
        expectThat(response.body["uuid"]).isEqualTo(interview.id.toString())
    }

    @Test
    @Order(6)
    fun `should set hosts emails`() {
        EmailServiceStub.stubForSendEmail()

        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.hosts.size).isEqualTo(0)
        val hosts = setOf("a@host.com", "b@host.com")

        var response = httpRequest(
            "/api/interview/${interview.id}/set_hosts",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
            body = mapOf(
                "hostsMails" to hosts
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        interview = interviewRepository.findAll().first()

        expectThat(interview.hosts.toList()).containsExactlyInAnyOrder(hosts)
    }

    @Test
    @Order(7)
    fun `should return unauthorized when trying to set hosts emails`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.hosts.size).isEqualTo(0)

        val response = httpRequest(
            "/api/interview/${interview.id}/set_hosts",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobseeker.user.mail, password)),
            body = mapOf(
                "hostsMails" to setOf("unauthorized@mail.com")
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.hosts.size).isEqualTo(0)
    }


    @Test
    @Order(8)
    fun `should set meeting length`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.minutesLength).isNull()
        val length = 30

        var response = httpRequest(
            "/api/interview/${interview.id}/set_duration",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
            body = mapOf("minutesLength" to length)
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        interview = interviewRepository.findAll().first()

        expectThat(interview.minutesLength).isEqualTo(length)
    }

    @Test
    @Order(9)
    fun `should return unauthorized when trying to set meeting length`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.minutesLength).isNull()
        val length = 30

        val response = httpRequest(
            "/api/interview/${interview.id}/set_duration",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobseeker.user.mail, password)),
            body = mapOf("minutesLength" to length)
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.hosts.size).isEqualTo(0)
    }

    @Test
    @Order(10)
    fun `should be able to pick date`() {
        EmailServiceStub.stubForSendEmail()

        var interview = Interview(null, null, null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.dateTime).isNull()
        val dateTime = Timestamp.from(Instant.MIN)

        var response = httpRequest(
            "/api/interview/${interview.id}/pick_date",
            method = HttpMethod.PUT,
            body = mapOf(
                "dateTime" to dateTime
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        interview = interviewRepository.findAll().first()

        expectThat(interview.dateTime.toString()).isEqualTo(dateTime.toString())
    }

    private val password = "a"

    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }
}
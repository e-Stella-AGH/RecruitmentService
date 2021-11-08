package org.malachite.estella

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.decodeBase64
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.interview.api.JobseekerName
import org.malachite.estella.interview.domain.InterviewDTO
import org.malachite.estella.interview.domain.InterviewNoteDTO
import org.malachite.estella.interview.domain.InterviewRepository
import org.malachite.estella.offer.domain.OfferRepository
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.OrganizationResponse
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.domain.*
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskResultRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.web.client.HttpStatusCodeException
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Date
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(
    initializers = [BaseIntegration.Companion.PropertyOverride::class],
    classes = [DemoApplication::class]
)
class BaseIntegration {

    private val file = Files.readAllBytes(Paths.get("src/main/kotlin/org/malachite/estella/stop-cv-format.pdf"))
    private val encodedFile: String = Base64.getEncoder().encodeToString(file)

    final fun getJobSeekerFilePayload(fileName: String, id: Int? = null): JobSeekerFilePayload =
        JobSeekerFilePayload(id, fileName, encodedFile)

    @Test
    fun `test for httpRequest`() {
        //when http request is sent
        val response = httpRequest(
            path = "/_meta/health",
            method = HttpMethod.GET
        )
        response.body as Map<String, Any>

        //then response is what we want it to be
        expect {
            that(response.statusCode).isEqualTo(HttpStatus.OK)
            that(response.body).isEqualTo(mapOf("text" to "App works"))
        }
    }

    data class Response(
        val statusCode: HttpStatus,
        val body: Any,
        val headers: HttpHeaders?
    )

    fun Map<String, Any>.toUser() =
        User(
            this["id"] as Int?,
            this["firstName"] as String,
            this["lastName"] as String,
            this["mail"] as String,
            this["password"] as String?
        )

    fun Map<String, Any>.toOrganization() =
        Organization(
            UUID.fromString(this["id"] as String),
            this["name"] as String,
            (this["user"] as Map<String, Any>).toUser(),
            this["verified"] as Boolean
        )

    fun Map<String, Any>.toOrganizationResponse() =
        OrganizationResponse(
            this["name"] as String,
        )

    fun Map<String, Any>.toOfferResponse() =
        OfferResponse(
            this["id"] as Int,
            this["name"] as String,
            this["description"] as String,
            this["position"] as String,
            (this["minSalary"] as Int).toLong(),
            (this["maxSalary"] as Int).toLong(),
            this["localization"] as String,
            (this["organization"] as Map<String, Any>).toOrganizationResponse(),
            (this["skills"] as ArrayList<Map<String, Any>>).toDesiredSkillSet(),
            (this["creator"] as Map<String, Any>).toHrPartner()
        )

    fun ArrayList<Map<String, Any>>.toDesiredSkillSet() =
        this.map { it.toDesiredSkill() }
            .toHashSet()

    fun Map<String, Any>.toDesiredSkill() =
        DesiredSkill(
            this["id"] as Int,
            this["name"] as String,
            (this["level"] as String).toSkillLevel()!!
        )

    var simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")

    fun Map<String, Any>.toApplicationDTO() =
        ApplicationDTO(
            this["id"] as Int,
            Date.valueOf(this["applicationDate"] as String),
            ApplicationStatus.valueOf(this["status"] as String),
            (this["stage"] as Map<String, Any>).toRecruitmentStage(),
            (this["jobSeeker"] as Map<String, Any>).toJobSeekerDTO(),
            (this["seekerFiles"] as List<Map<String, Any>>).map { it.toJobSeekerFileDto() }.toSet(),
            (this["applicationStages"] as List<Int>)
        )

    fun Map<String, Any>.toApplicationDTOWithStagesAndOfferName() =
        ApplicationDTOWithStagesListAndOfferName(
            this["id"] as Int,
            Date.valueOf(this["applicationDate"] as String),
            ApplicationStatus.valueOf(this["status"] as String),
            (this["stage"] as Map<String, Any>).toRecruitmentStage(),
            (this["jobSeeker"] as Map<String, Any>).toJobSeekerDTO(),
            setOf(),
            (this["stages"] as List<Map<String, Any>>).map { it.toRecruitmentStage() },
            this["offerName"] as String
        )

    fun Map<String, Any>.toApplicationForDevDTO() =
            ApplicationForDevDTO(
                    (this["application"] as Map<String, Any>).toApplicationDTO(),
                    this["taskStageUUID"] as String,
                    (this["notes"] as List<Map<String, Any>>).map { it.toApplicationNoteDTO() }.toSet(),
                    this["position"] as String
            )

    fun String.toSkillLevel(): SkillLevel? {
        return when (this) {
            "NICE_TO_HAVE" -> SkillLevel.NICE_TO_HAVE
            "JUNIOR" -> SkillLevel.JUNIOR
            "REGULAR" -> SkillLevel.REGULAR
            "ADVANCED" -> SkillLevel.ADVANCED
            "MASTER" -> SkillLevel.MASTER
            else -> null
        }
    }

    fun Map<String, Any>.toHrPartner() = HrPartner(
        this["id"] as Int?,
        (this["organization"] as Map<String, Any>).toOrganization(),
        (this["user"] as Map<String, Any>).toUser()
    )

    fun Map<String, Any>.toJobSeeker() = JobSeeker(
        this["id"] as Int?,
        (this["user"] as Map<String, Any>).toUser(),
        mutableSetOf()
    )

    fun Map<String, Any>.toJobSeekerDTO() =
        JobSeekerDTO(this["id"] as Int, (this["user"] as Map<String, Any>).toUserDTO())

    fun Map<String, Any>.toHrPartnerResponse() =
        HrPartnerResponse(
            this["organizationName"] as String,
            (this["user"] as Map<String, Any>).toUserDTO()
        )

    fun Map<String, Any>.toUserDTO() =
        UserDTO(
            this["id"] as Int,
            this["firstName"] as String,
            this["lastName"] as String,
            this["mail"] as String
        )

    fun Map<String, Any>.toRecruitmentProcessDto() =
        RecruitmentProcessDto(
            this["id"] as Int?,
            (this["startDate"] as String?)?.let { Date.valueOf(it) },
            (this["endDate"] as String?)?.let { Date.valueOf(it) },
            (this["offer"] as Map<String, Any>).toOfferResponse(),
            (this["stages"] as List<Map<String, Any>>).toRecruitmentStagesList(),
        )

    fun Map<String, Any>.toJobSeekerFileDto() =
        JobSeekerFileDTO(
            this["id"] as Int,
            this["fileName"] as String,
            this["fileBase64"] as String
        )

    fun List<Map<String, Any>>.toRecruitmentStagesList() =
        this.map { it.toRecruitmentStage() }

    fun Map<String, Any>.toRecruitmentStage() = RecruitmentStage(
        this["id"] as Int?,
        StageType.valueOf(this["type"] as String)
    )

    fun List<Map<String, Any>>.toTaskDto() =
        this.map { it.toTaskDto() }

    fun Map<String, Any>.toTaskDto() = TaskDto(
        id = this["id"] as Int?,
        testsBase64 = this["testsBase64"] as String,
        descriptionFileName = this["descriptionFileName"] as String,
        descriptionBase64 = this["descriptionBase64"] as String,
        timeLimit = this["timeLimit"] as Int,
    )

    fun Map<String, Any>.toApplicationNoteDTO() = ApplicationNoteDTO(
        author = this["author"] as String,
        tags = this["tags"] as List<String>,
        text = (this["text"] as String).decodeBase64()
    )

    fun Map<String, Any>.toApplicationNotes() = ApplicationNotes(
        notes = (this["notes"] as List<Map<String, Any>>).map { it.toApplicationNoteDTO() }
    )

    fun Map<String, Any>.toTaskResultWithTestDTO(): TaskResultWithTestDTO = TaskResultWithTestDTO(
        code = (this["code"] as String).decodeBase64(),
        results = (this["results"] as String).decodeBase64(),
        tests = (this["tests"] as String).decodeBase64(),
        description = (this["tests"] as String).decodeBase64()
    )

    fun Map<String, Any>.toTasksNotesDTO(): TasksNotesDTO = TasksNotesDTO(
        tasks = (this["tasks"] as List<Map<String, Any>>).map { it.toTaskResultWithTestDTO() },
        notes = (this["notes"] as List<Map<String,Any>>).map{it.toApplicationNoteDTO()}
    )

    fun String.toTimestamp(): Timestamp {
        val dateTime = ZonedDateTime.parse(this).toLocalDateTime().atZone(ZoneId.of("+01:00")).toLocalDateTime()
        return Timestamp.valueOf(dateTime)
    }

    fun Map<String, Any>.toJobSeekerNameDTO() = JobseekerName(
        firstName = this["firstName"] as String,
        lastName = this["lastName"] as String
    )

    fun Map<String, Any>.toInterviewDTO() = InterviewDTO(
        this["id"] as String?,
        (this["dateTime"] as String?)?.toTimestamp(),
        this["minutesLength"] as Int,
        (this["application"] as Map<String, Any>).toApplicationDTO(),
        this["hosts"] as Set<String>?
    )

    fun List<Map<String, Any>>.toInterviewNotesDTO() =
        this.map { it.toInterviewNoteDTO() }.toSet()

    fun Map<String, Any>.toInterviewNoteDTO() =
        InterviewNoteDTO(this["id"] as Int, this["note"] as String)


    fun applyForOffer(jobSeeker: JobSeeker, password: String, offer: Offer): Response = httpRequest(
        path = "/api/applications/apply/${offer.id}/user",
        method = HttpMethod.POST,
        headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobSeeker.user.mail, password)),
        body = mapOf("files" to setOf<JobSeekerFilePayload>())
    )

    fun updateStage(applicationId: Int, mail: String, password: String) = httpRequest(
        path = "/api/applications/${applicationId}/next",
        method = HttpMethod.PUT,
        headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail, password)),
    )

    fun updateStage(applicationId: Int, mail: String, password: String, devs: Set<String>) = httpRequest(
            path = "/api/applications/${applicationId}/next",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail, password)),
            body = mapOf("devs" to devs)
    )

    private fun loginUser(userMail: String, userPassword: String): Response = httpRequest(
        path = "/api/users/login",
        method = HttpMethod.POST,
        body = mapOf(
            "mail" to userMail,
            "password" to userPassword
        )
    )

    fun getAuthToken(mail: String, password: String): String =
        loginUser(mail, password).headers!![EStellaHeaders.authToken]!![0]

    fun eventually(fn: () -> Unit) {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(fn)
    }

    fun coolDown(fn: () -> Unit) {
        await().during(Duration.ofSeconds(1)).untilAsserted(fn)
    }

    companion object {
        class PropertyOverride : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "mail_service_url=http://localhost:9797",
                    "admin_api_key=API_KEY",
                    "should_fake_load=false",
                    "cloud_amqp_url=amqp://localhost:5672"
                )
            }
        }

        private val restTemplate = TestRestTemplate()
        val objectMapper = jacksonObjectMapper()

        fun httpRequest(
            path: String,
            method: HttpMethod,
            headers: Map<String, String> = mapOf(),
            body: Map<String, Any> = mapOf()
        ): Response {
            val httpHeaders = HttpHeaders().also {
                headers.forEach { (key, value) -> it.add(key, value) }
            }
            val uri = URI.create("http://localhost:8080$path")

            val requestEntity = RequestEntity(body, httpHeaders, method, uri)

            return try {
                val response = restTemplate.exchange(requestEntity, String::class.java)
                val statusCode = response.statusCode
                val responseBody = objectMapper.readValue(response.body, Any::class.java)
                Response(statusCode, responseBody, response.headers)
            } catch (exception: HttpStatusCodeException) {
                val responseBody = objectMapper.readValue(exception.responseBodyAsString, Any::class.java)
                val statusCode = exception.statusCode
                Response(statusCode, responseBody, exception.responseHeaders)
            }
        }
    }


    @Autowired
    lateinit var interviewRepository: InterviewRepository

    @Autowired
    lateinit var jobSeekerRepository: JobSeekerRepository

    @Autowired
    lateinit var recruitmentStageRepository: RecruitmentStageRepository

    @Autowired
    lateinit var applicationRepository: ApplicationRepository

    @Autowired
    lateinit var applicationStageDataRepository: ApplicationStageRepository

    @Autowired
    lateinit var taskStageRepository: TaskStageRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var taskResultRepository: TaskResultRepository

    @Autowired
    lateinit var securityService: SecurityService

    @Autowired
    lateinit var recruitmentProcessService: RecruitmentProcessService

    @Autowired
    lateinit var offerRepository: HibernateOfferRepository

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    @Autowired
    lateinit var tasksRepository: TaskRepository


}
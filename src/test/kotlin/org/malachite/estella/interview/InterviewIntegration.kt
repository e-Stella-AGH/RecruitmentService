package org.malachite.estella.interview

import org.aspectj.lang.annotation.After
import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.interview.api.MeetingDate
import org.malachite.estella.interview.api.NotesFilePayload
import org.malachite.estella.interview.domain.InterviewRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.people.seekers.DummyJobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentProcessRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.jobSeekers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.event.annotation.AfterTestExecution
import org.springframework.test.context.event.annotation.BeforeTestMethod
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.sql.rowset.serial.SerialClob
import kotlin.math.exp

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class InterviewIntegration: BaseIntegration() {

    @Autowired
    private lateinit var interviewRepository: InterviewRepository
    @Autowired
    private lateinit var jobSeekerRepository: JobSeekerRepository
    @Autowired
    private lateinit var recruitmentStageRepository: RecruitmentStageRepository
    @Autowired
    private lateinit var applicationRepository: ApplicationRepository
    @Autowired
    private lateinit var recruitmentProcessRepository: RecruitmentProcessRepository

    private lateinit var application: Application
    private lateinit var jobseeker: JobSeeker
    private lateinit var hrPartner: HrPartner

    @BeforeEach
    fun prepareApplication() {
        val jobseeker = jobSeekerRepository.findAll().first()

        val stage = recruitmentStageRepository.findAll().first()
        hrPartner = recruitmentProcessRepository.findAll().first{ it.stages.contains(stage) }.offer.creator
        val application = Application(null,
                Date(Calendar.getInstance().time.time),
                ApplicationStatus.IN_PROGRESS,
                stage,
                jobseeker,
                setOf(),
                setOf(),
                setOf(),
                setOf())
        applicationRepository.save(application)
        this.application = application
        this.jobseeker = jobseeker
    }

    @AfterEach
    fun clearApplication() {
        applicationRepository.deleteById(application.id!!)
        interviewRepository.findAll().forEach { interviewRepository.deleteById(it.id!!) }
    }

    @Test
    @Order(1)
    fun `should return jobseeker name`() {
        val interview = Interview(null, null, 30, application, listOf(), setOf())
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
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = Interview(null, Timestamp.valueOf(LocalDateTime.now()), 60, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first { it.minutesLength == 60 }
        val response = httpRequest(
                "/api/interview/newest/${application.id}",
                method = HttpMethod.GET
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as Map<String, String>
        expectThat(response.body["interviewId"]).isEqualTo(interview.id.toString())
    }

    @Test
    @Order(5)
    fun `should return interview with later date when new date is null`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = Interview(null, null, 60, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first { it.minutesLength == 60 }
        val response = httpRequest(
                "/api/interview/newest/${application.id}",
                method = HttpMethod.GET
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        response.body as Map<String, Any>
        expectThat(response.body["interviewId"]).isEqualTo(interview.id.toString())
    }

    @Test
    @Order(6)
    fun `should set hosts emails`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first()
        expectThat(interview.hosts.size).isEqualTo(0)
        val hosts = listOf("a@host.com", "b@host.com")

        var response = httpRequest(
                "/api/interview/${interview.id}/set-hosts",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
                body = mapOf(
                        "hostsMails" to hosts
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        response = httpRequest(
                "/api/interview/${interview.id}/set-hosts",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobseeker.user.mail, password)),
                body = mapOf(
                        "hostsMails" to listOf("unauthorized@mail.com")
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.hosts.toString()).isEqualTo(hosts.toString())
    }

    @Test
    @Order(7)
    fun `should return unauthorized when trying to set hosts emails`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first()
        expectThat(interview.hosts.size).isEqualTo(0)

        val response = httpRequest(
                "/api/interview/${interview.id}/set-hosts",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobseeker.user.mail, password)),
                body = mapOf(
                        "hostsMails" to listOf("unauthorized@mail.com")
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.hosts.size).isEqualTo(0)
    }

    @Test
    @Order(8)
    fun `should be able to pick date`() {
        var interview = Interview(null, null, 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first()
        expectThat(interview.dateTime).isNull()
        val dateTime = Timestamp.from(Instant.MIN)

        var response = httpRequest(
                "/api/interview/${interview.id}/pick-date",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(jobseeker.user.mail, password)),
                body = mapOf(
                        "dateTime" to dateTime
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        response = httpRequest(
                "/api/interview/${interview.id}/pick-date",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
                body = mapOf(
                        "dateTime" to Timestamp.from(Instant.MIN)
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.dateTime.toString()).isEqualTo(dateTime.toString())
    }

    @Test
    @Order(9)
    fun `should return unaouthorized when trying to pick date`() {
        var interview = Interview(null, null, 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first()
        expectThat(interview.dateTime).isNull()
        val dateTime = Timestamp.from(Instant.MIN)

        val response = httpRequest(
                "/api/interview/${interview.id}/pick-date",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
                body = mapOf(
                        "dateTime" to Timestamp.from(Instant.MIN)
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.dateTime).isNull()
    }

    @Test
    @Order(10)
    fun `should be able to set notes`() {
        var interview = Interview(null, Timestamp.valueOf(LocalDateTime.MIN), 30, application, listOf(), setOf())
        interviewRepository.save(interview)
        interview = interviewRepository.findAll().first()
        expectThat(interview.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val notes = setOf(NotesFilePayload(null, noteA), NotesFilePayload(null, noteB))

        var response = httpRequest(
                "/api/interview/${interview.id}/add-notes",
                method = HttpMethod.PUT,
                headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, password)),
                body = mapOf(
                        "notes" to notes
                )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        interview = interviewRepository.findAll().first()

        expectThat(interview.notes.size).isEqualTo(2)
        val interviewNoteA = interview.notes.elementAt(0).note.characterStream.readText()
        val interViewNoteB = interview.notes.elementAt(1).note.characterStream.readText()

        expectThat(listOf(this.noteA, this.noteB))
                .containsExactlyInAnyOrder(listOf(interviewNoteA, interViewNoteB))
    }



    private fun loginUser(userMail: String, userPassword: String = password): Response {
        return httpRequest(
                path = "/api/users/login",
                method = HttpMethod.POST,
                body = mapOf(
                        "mail" to userMail,
                        "password" to userPassword
                )
        )
    }

    private fun getAuthToken(mail: String, password: String):String =
            loginUser(mail, password).headers!![EStellaHeaders.authToken]!![0]

    private val password = "a"
    private val noteA = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Mauris sit amet erat bibendum, condimentum ex vehicula, rhoncus magna. " +
            "Mauris mattis, sem non aliquam fermentum, dolor eros porttitor ligula, " +
            "at semper diam lorem in libero. Integer imperdiet felis at arcu suscipit iaculis. " +
            "Maecenas pellentesque egestas nunc, aliquet mattis enim egestas in. " +
            "Cras quis facilisis lorem. Aenean sed varius odio. " +
            "Ut eget orci at ante lobortis interdum vel sit amet dolor." +
            " Praesent varius blandit tortor ac condimentum. Aenean congue odio sem, " +
            "a egestas eros sollicitudin ac. Phasellus rutrum enim at mi eleifend tincidunt." +
            " Duis ac ligula arcu. Integer ultricies iaculis pretium. Duis et molestie purus. "
    private val noteB = "Nam quis lectus massa. Praesent vehicula arcu quis rutrum consectetur. " +
            "Vestibulum mattis turpis in tortor ornare iaculis. Nam enim mauris, " +
            "iaculis id iaculis et, porta in neque. Aenean sit amet blandit augue. " +
            "Integer nec nibh nec est viverra varius. Nam in pellentesque ante. "



    private fun withStatusAndMessage(response: Response, message: String, status: HttpStatus) {
        expectThat(response.statusCode).isEqualTo(status)
        response.body as Map<String, String>
        expectThat(response.body["message"]).isEqualTo(message)
    }
}
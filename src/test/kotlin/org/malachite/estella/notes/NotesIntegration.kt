package org.malachite.estella.notes

import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.*
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.encodeToBase64
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.commons.toBlob
import org.malachite.estella.commons.toClob
import org.malachite.estella.interview.api.NotesFilePayload
import org.malachite.estella.interview.domain.InterviewRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskResultRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotesIntegration : BaseIntegration() {


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
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var taskResultRepository: TaskResultRepository

    @Autowired
    private lateinit var securityService: SecurityService

    @Autowired
    private lateinit var recruitmentProcessService: RecruitmentProcessService

    private lateinit var application: Application
    private lateinit var applicationStageData: ApplicationStageData
    private lateinit var organization: Organization
    private lateinit var jobseeker: JobSeeker
    private lateinit var hrPartner: HrPartner
    private lateinit var zonedDateTime: ZonedDateTime


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

        zonedDateTime = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault())
        val task = taskRepository.findAll().first()
        val taskResult = TaskResult(
            null, "xd".encodeToBase64().toBlob(), "xd".encodeToBase64().toClob(),
            Timestamp.valueOf(zonedDateTime.toLocalDateTime()), null, task, taskStage = taskStage
        )
            .let { taskResultRepository.save(it) }

        taskStage = taskStageRepository.save(taskStage.copy(tasksResult = listOf(taskResult)))

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
    fun `should be able to set notes and then add new note and then get all notes - to interview`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val author = "test@test.com"
        val tags = setOf<String>("Git")
        val notes = setOf(NotesFilePayload(null, noteA, tags, author), NotesFilePayload(null, noteB, tags, author))
        val password = securityService.hashOrganization(organization, applicationStageData.tasksStage!!)

        val firstResponse = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to password),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(firstResponse.statusCode).isEqualTo(HttpStatus.OK)

        interview = interviewRepository.findAll().first()

        val stage = interview.applicationStage

        checkFirstPut(stage, author, tags)


//      Second part of test adding next notes
        val newAuthor = "test2@test2.com"
        val newTags = setOf<String>("Bad answer")
        val newNotes = setOf(NotesFilePayload(null, noteA, newTags, newAuthor))

        val secondResponse = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to password),
            body = mapOf(
                "notes" to newNotes
            )
        )

        expectThat(secondResponse.statusCode).isEqualTo(HttpStatus.OK)

        //      Third part get notes from endpoint


        val thirdResponse = httpRequest(
            "/api/applications/get_notes?interview_note=${interview.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to password),
        )

        checkGetResponse(thirdResponse.statusCode, thirdResponse.body as Map<String, Any>, author, newAuthor, tags, newTags)
    }

    @Test
    @Order(2)
    fun `should return unauthorized when trying to set notes - interview`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val notes = setOf(
            NotesFilePayload(null, noteA, setOf("Git"), "test@test.com"),
            NotesFilePayload(null, noteB, setOf("Git"), "test@test.com")
        )
        val badPassword = "xd"

        val response = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to badPassword),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
    }

    @Test
    @Order(3)
    fun `should be able to set notes and then add new note - to cv`() {
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val author = "test@test.com"
        val tags = setOf<String>("Git")
        val notes = setOf(NotesFilePayload(null, noteA, tags, author), NotesFilePayload(null, noteB, tags, author))

        val firstResponse = httpRequest(
            "/api/applications/add_notes?cv_note=${applicationStageData.application.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, hrPassword)),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(firstResponse.statusCode).isEqualTo(HttpStatus.OK)

        var appplicationFromDB = applicationRepository.findById(application.id!!).get()
        var stage = appplicationFromDB.applicationStages.first()

        checkFirstPut(stage, author, tags)


//      Second part of test adding next notes
        val newAuthor = "test2@test2.com"
        val newTags = setOf<String>("Bad answer")
        val newNotes = setOf(NotesFilePayload(null, noteA, newTags, newAuthor))

        val secondResponse = httpRequest(
            "/api/applications/add_notes?cv_note=${applicationStageData.application.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, hrPassword)),
            body = mapOf(
                "notes" to newNotes
            )
        )

        expectThat(secondResponse.statusCode).isEqualTo(HttpStatus.OK)

        //      Third part get notes from endpoint

        val thirdResponse = httpRequest(
            "/api/applications/get_notes?cv_note=${applicationStageData.application.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, hrPassword))
        )

        checkGetResponse(thirdResponse.statusCode, thirdResponse.body as Map<String, Any>, author, newAuthor, tags, newTags)
    }


    @Test
    @Order(4)
    fun `should return unauthorized when trying to set notes - to cv`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val notes = setOf(
            NotesFilePayload(null, noteA, setOf("Git"), "test@test.com"),
            NotesFilePayload(null, noteB, setOf("Git"), "test@test.com")
        )
        val badPassword = "xd"

        val response = httpRequest(
            "/api/applications/add_notes?cv_note=${applicationStageData.application.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to badPassword),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
    }

    @Test
    @Order(5)
    fun `should be able to set notes and then add new note - to taskStage`() {
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val author = "test@test.com"
        val tags = setOf<String>("Git")
        val notes = setOf(NotesFilePayload(null, noteA, tags, author), NotesFilePayload(null, noteB, tags, author))
        val password = securityService.hashOrganization(organization, applicationStageData.tasksStage!!)


        val firstResponse = httpRequest(
            "/api/applications/add_notes?task_note=${applicationStageData.tasksStage!!.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to password),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(firstResponse.statusCode).isEqualTo(HttpStatus.OK)

        val stage = applicationStageDataRepository.findById(applicationStageData.id!!).get()

        checkFirstPut(stage, author, tags)


//      Second part of test adding next notes
        val newAuthor = "test2@test2.com"
        val newTags = setOf<String>("Bad answer")
        val newNotes = setOf(NotesFilePayload(null, noteA, newTags, newAuthor))

        val secondResponse = httpRequest(
            "/api/applications/add_notes?task_note=${applicationStageData.tasksStage!!.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to password),
            body = mapOf(
                "notes" to newNotes
            )
        )

        expectThat(secondResponse.statusCode).isEqualTo(HttpStatus.OK)

        //      Third part get notes with task from endpoint

        val thirdResponse = httpRequest(
            "/api/applications/get_notes?task_note=${applicationStageData.tasksStage!!.id}&with_tasks=true",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to password)
        )

        val body = thirdResponse.body as Map<String, Any>

        checkGetResponse(
            thirdResponse.statusCode, body,
            author, newAuthor, tags, newTags
        )


        val tasksResponse = (body["tasks"] as List<Map<String, Any>>).map { it.toTaskResultWithTestDTO() }
        expectThat(tasksResponse[0].code).isEqualTo("xd")
        expectThat(tasksResponse[0].results).isEqualTo("xd")
    }


    @Test
    @Order(6)
    fun `should return unauthorized when trying to set notes - to task`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val notes = setOf(
            NotesFilePayload(null, noteA, setOf("Git"), "test@test.com"),
            NotesFilePayload(null, noteB, setOf("Git"), "test@test.com")
        )
        val badPassword = "xd"

        val response = httpRequest(
            "/api/applications/add_notes?task_note=${applicationStageData.tasksStage!!.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to badPassword),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
    }

    @Test
    @Order(7)
    fun `should be able to set notes and then add new note - to interview - hr`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val author = "test@test.com"
        val tags = setOf<String>("Git")
        val notes = setOf(NotesFilePayload(null, noteA, tags, author), NotesFilePayload(null, noteB, tags, author))
        val password = securityService.hashOrganization(organization, applicationStageData.tasksStage!!)

        val firstResponse = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, hrPassword)),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(firstResponse.statusCode).isEqualTo(HttpStatus.OK)
        interview = interviewRepository.findAll().first()
        val stage = interview.applicationStage
        checkFirstPut(stage, author, tags)


//      Second part of test adding next notes
        val newAuthor = "test2@test2.com"
        val newTags = setOf<String>("Bad answer")
        val newNotes = setOf(NotesFilePayload(null, noteA, newTags, newAuthor))

        val secondResponse = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.devPassword to password),
            body = mapOf(
                "notes" to newNotes
            )
        )

        expectThat(secondResponse.statusCode).isEqualTo(HttpStatus.OK)

        //      Third part get notes with task from endpoint

        val thirdResponse = httpRequest(
            "/api/applications/get_notes?interview_note=${interview.id}",
            method = HttpMethod.GET,
            headers = mapOf(EStellaHeaders.devPassword to password),
        )

        checkGetResponse(thirdResponse.statusCode, thirdResponse.body as Map<String,Any>, author, newAuthor, tags, newTags)
    }


    @Test
    @Order(8)
    fun `should return unauthorized when trying to set notes - hr interview`() {
        var interview =
            Interview(null, Timestamp.valueOf(LocalDateTime.MIN), null, applicationStageData, setOf())
        interview = interviewRepository.save(interview)
        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
        val noteA = String(Base64.getEncoder().encode(noteA.encodeToByteArray()))
        val noteB = String(Base64.getEncoder().encode(noteB.encodeToByteArray()))
        val notes = setOf(
            NotesFilePayload(null, noteA, setOf("Git"), "test@test.com"),
            NotesFilePayload(null, noteB, setOf("Git"), "test@test.com")
        )
        val badPassword = "xd"

        val response = httpRequest(
            "/api/applications/add_notes?interview_note=${interview.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to badPassword),
            body = mapOf(
                "notes" to notes
            )
        )

        expectThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        interview = interviewRepository.findAll().first()

        expectThat(interview.applicationStage.notes.size).isEqualTo(0)
    }

    private fun checkFirstPut(stage: ApplicationStageData, author: String, tags: Set<String>) {
        expectThat(stage.notes.size).isEqualTo(2)
        var interviewNoteA = stage.notes.elementAt(0).text!!.characterStream.readText()
        var interviewNoteB = stage.notes.elementAt(1).text!!.characterStream.readText()
        expectThat(listOf(this.noteA, this.noteB))
            .containsExactlyInAnyOrder(listOf(interviewNoteA, interviewNoteB))
        var authors = stage.notes.map { it.author }
        expectThat(authors).containsExactlyInAnyOrder(listOf(author, author))
        var noteTags: List<String> = stage.notes.flatMap { it.tags.map { it.text } }
        expectThat(noteTags).containsExactlyInAnyOrder(listOf(tags, tags).flatMap { it })
    }


    private fun checkGetResponse(
        status: HttpStatus,
        body: Map<String, Any>,
        author: String,
        newAuthor: String,
        tags: Set<String>,
        newTags: Set<String>
    ) {
        expectThat(status).isEqualTo(HttpStatus.OK)


        val responseBody = body.toApplicationNotes()

        expectThat(responseBody.notes.size).isEqualTo(3)
        val savedInterviewNoteA = responseBody.notes.elementAt(0).text
        val savedInterviewNoteB = responseBody.notes.elementAt(1).text
        val savedInterviewNoteC = responseBody.notes.elementAt(2).text
        expectThat(listOf(this.noteA, this.noteB, this.noteA))
            .containsExactlyInAnyOrder(listOf(savedInterviewNoteA, savedInterviewNoteB, savedInterviewNoteC))
        val authors = responseBody.notes.map { it.author }
        expectThat(authors).containsExactlyInAnyOrder(listOf(author, author, newAuthor))
        val noteTags = responseBody.notes.flatMap { it.tags }
        expectThat(noteTags).containsExactlyInAnyOrder(listOf(tags, tags, newTags).flatMap { it })
    }

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


    private val hrPassword = "a"
}
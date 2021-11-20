package org.malachite.estella.queue

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.models.interviews.Interview
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.queues.utils.InterviewResultRabbitDTO
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class InterviewDateTest : BaseIntegration() {
    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate


    private lateinit var applicationStageData: ApplicationStageData

    @BeforeEach
    fun prepareInterview() {
        EmailServiceStub.stubForSendEmail()

        val jobseeker = jobSeekerRepository.findAll().first()

        val stage = recruitmentStageRepository.findAll().first()
        val application = Application(
            null,
            Date(Calendar.getInstance().time.time),
            ApplicationStatus.IN_PROGRESS,
            jobseeker,
            mutableSetOf(),
            mutableListOf()
        )
        val savedApplication = applicationRepository.save(application)
        val applicationStageData = ApplicationStageData(null, stage, savedApplication, null, null, setOf(), mutableSetOf())
        this.applicationStageData = applicationStageDataRepository.save(applicationStageData)
    }

    @Test
    @Order(1)
    fun `interview date result consuming`() {
        var interview = Interview(null, null, null, applicationStageData)
            .let { interviewRepository.save(it) }

        expectThat(interview.minutesLength).isEqualTo(null)
        expectThat(interview.dateTime).isEqualTo(null)

        publish(interview.id!!)

        eventually {
            interview = interviewRepository.findById(interview.id!!).get()
            expectThat(interview.minutesLength).isEqualTo(20)
            expectThat(interview.dateTime).isEqualTo(Timestamp.from(Instant.ofEpochMilli(1655979300000)))
        }
    }

    @Test
    @Order(2)
    fun `interview bad message arguments consuming`() {
        var interview = Interview(null, null, null, applicationStageData)
            .let { interviewRepository.save(it) }

        expectThat(interview.minutesLength).isEqualTo(null)
        expectThat(interview.dateTime).isEqualTo(null)


        badPublish()

        coolDown {
            interview = interviewRepository.findById(interview.id!!).get()
            expectThat(interview.minutesLength).isEqualTo(null)
            expectThat(interview.dateTime).isEqualTo(null)
        }
    }

    @Test
    @Order(3)
    fun `interview message with bad parameters consuming`() {
        var interview = Interview(null, null, null, applicationStageData)
            .let { interviewRepository.save(it) }

        expectThat(interview.minutesLength).isEqualTo(null)
        expectThat(interview.dateTime).isEqualTo(null)

        publishWithMistake()

        coolDown {
            interview = interviewRepository.findById(interview.id!!).get()
            expectThat(interview.minutesLength).isEqualTo(null)
            expectThat(interview.dateTime).isEqualTo(null)
        }
    }

    @Test
    @Order(4)
    fun `interview message with bad interview length consuming`() {
        var interview = Interview(null, null, null, applicationStageData)
            .let { interviewRepository.save(it) }

        expectThat(interview.minutesLength).isEqualTo(null)
        expectThat(interview.dateTime).isEqualTo(null)

        badPublishInterviewLength(interview.id!!)

        coolDown {
            interview = interviewRepository.findById(interview.id!!).get()
            expectThat(interview.minutesLength).isEqualTo(null)
            expectThat(interview.dateTime).isEqualTo(null)
        }
    }

    fun publish(uuid: UUID) {
        val resultBody = mapOf(
            "meetingUUID" to uuid.toString(),
            "meetingDate" to "1655979300000",
            "meetingLength" to "20"
        )
        send(resultBody)
    }

    fun badPublish() {
        val resultBody = mapOf(
            "meetingUUID" to "1",
            "meetingDate" to "yes",
            "meetingLength" to "-1"
        )
        send(resultBody)
    }

    fun badPublishInterviewLength(uuid: UUID) {
        val resultBody = mapOf(
            "meetingUUID" to uuid.toString(),
            "meetingDate" to "1655979300000",
            "meetingLength" to "-1"
        )
        send(resultBody)
    }

    fun publishWithMistake() {
        val resultBody = mapOf(
            "results" to "xd",
            "code" to "xd",
            "taskId" to "4"
        )
        send(resultBody)
    }

    private fun send(msg: Map<String, String>) = Json.encodeToString(msg).let {
        rabbitTemplate.send("interview", Message(Base64.getEncoder().encode(it.toByteArray()), MessageProperties()))
    }
}
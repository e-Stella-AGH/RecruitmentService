package org.malachite.estella.queue

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.util.DatabaseReset
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TaskResultTest : BaseIntegration() {
    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    private lateinit var task: Task
    private lateinit var taskStage: TaskStage
    private lateinit var taskResult: TaskResult

    @BeforeEach
    fun prepareTask() {
        task = Task(null, SerialBlob("xd".toByteArray()), SerialClob("xd".toCharArray()), "filename", 10)
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
        val applicationStageData =
            ApplicationStageData(null, stage, savedApplication, null, null, setOf(), mutableSetOf())
        val savedApplicationStageData = applicationStageDataRepository.save(applicationStageData)
        this.taskStage = TaskStage(null, savedApplicationStageData)
        taskStage = taskStageRepository.save(taskStage)
        task = taskRepository.save(task)
        taskResult = taskResultRepository.save(TaskResult(null, null, null, null, null, task, taskStage))
    }

    val now = Timestamp.from(Instant.now())

    @Test
    @Order(1)
    fun `task result consuming`() {
        expectThat(taskStage.tasksResult.size).isEqualTo(0)
        val xd1 = "xd"
        val xd2 = "xdd"
        val code = SerialClob(xd1.toCharArray())
        val results =  SerialBlob(xd1.toByteArray())
        var taskResult = taskResultRepository.findById(this.taskResult.id!!).get()
        taskResult = taskResult.copy(results = results, code = code, startTime = now, taskStage = taskResult.taskStage)

        publish(taskResult)

        eventually {
            taskStage = taskStageRepository.findById(taskStage.id!!).get()
            expectThat(taskStage.tasksResult.size).isEqualTo(1)
            expectThat(code.characterStream.readText()).isEqualTo(xd1)
            val savedResults = taskStage.tasksResult.first().results!!.binaryStream.readAllBytes()
            expectThat(Base64.getDecoder().decode(savedResults)).isEqualTo(xd1.toByteArray())
        }
        // Test if results is updated and not added as new

        val newTaskResult = taskResult.copy(code = SerialClob(xd2.toCharArray()), taskStage = taskResult.taskStage)
        publish(newTaskResult)
        eventually {
            taskStage = taskStageRepository.findById(taskStage.id!!).get()
            expectThat(taskStage.tasksResult.size).isEqualTo(1)
            expectThat(taskStage.tasksResult.first().code!!.characterStream.readText()).isEqualTo(xd2)
            val savedResults = taskStage.tasksResult.first().results!!.binaryStream.readAllBytes()
            expectThat(Base64.getDecoder().decode(savedResults)).isEqualTo(xd1.toByteArray())
        }
    }

    @Test
    @Order(2)
    fun `task bad message format consuming`() {
        expectThat(taskStage.tasksResult.size).isEqualTo(0)

        badPublish()

        coolDown {
            expectThat(taskStage.tasksResult.size).isEqualTo(0)
        }
    }

    @Test
    @Order(3)
    fun `task bad message parameter consuming`() {
        expectThat(taskStage.tasksResult.size).isEqualTo(0)

        publishWithMistake()

        coolDown {
            expectThat(taskStage.tasksResult.size).isEqualTo(0)
        }
    }


    fun publish(result: TaskResult) {
        val resultBody = mapOf(
            "results" to String(result.results!!.binaryStream.readAllBytes()),
            "code" to (result.code!!.characterStream!!.readText()),
            "solverId" to result.taskStage!!.id!!.toString(),
            "taskId" to result.task.id!!.toString()
        )
        send(resultBody)

    }

    fun badPublish() {
        val resultBody = mapOf(
            "results" to "4",
            "potatoes" to "yes",
            "solverId" to "1"
        )
        send(resultBody)
    }

    fun publishWithMistake() {
        // missing solverId
        val resultBody = mapOf(
            "results" to "xd",
            "code" to "xd",
            "taskId" to "4"
        )
        send(resultBody)
    }

    private fun send(msg: Map<String, String>) = Json.encodeToString(msg).let {
        rabbitTemplate.send("task_result", Message(Base64.getEncoder().encode(it.toByteArray()), MessageProperties()))
    }
}
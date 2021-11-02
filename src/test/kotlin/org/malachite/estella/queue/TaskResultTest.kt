package org.malachite.estella.queue

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.sql.Date
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

    @BeforeEach
    fun prepareTask() {
        task = Task(null, SerialBlob("xd".toByteArray()), SerialClob("xd".toCharArray()), "filename", 10)
        val jobseeker = jobSeekerRepository.findAll().first()

        val stage = recruitmentStageRepository.findAll().first()
        val application = Application(null,
                Date(Calendar.getInstance().time.time),
                ApplicationStatus.IN_PROGRESS,
                jobseeker,
                mutableSetOf(),
                mutableListOf())
        val savedApplication = applicationRepository.save(application)
        val applicationStageData = ApplicationStageData(null, stage, savedApplication, null, null, setOf())
        val savedApplicationStageData = applicationStageDataRepository.save(applicationStageData)
        this.taskStage = TaskStage(null, listOf(), savedApplicationStageData, mutableListOf())
        taskStage = taskStageRepository.save(taskStage)
        task = taskRepository.save(task)
    }

    @Test
    @Order(1)
    fun `task result consuming`() {
        expectThat(taskStage.tasksResult.size).isEqualTo(0)
        val xd1 = "xd"
        val xd2 = "xdd"
        val code = SerialClob(xd1.toCharArray())
        val results =  SerialBlob(xd1.toByteArray())

        val taskResult = TaskResult(null, results, code, null, null, task, taskStage)
        publish(taskResult)

        eventually {
            taskStage = taskStageRepository.findById(taskStage.id!!).get()
            expectThat(taskStage.tasksResult.size).isEqualTo(1)
            expectThat(code.characterStream.readText()).isEqualTo(xd1)
            val savedResults = taskStage.tasksResult[0].results!!.binaryStream.readAllBytes()
            expectThat(Base64.getDecoder().decode(savedResults)).isEqualTo(xd1.toByteArray())
        }
        // Test if results is updated and not added as new
        publish(taskResult.copy(code = SerialClob(xd2.toCharArray())))
        eventually {
            taskStage = taskStageRepository.findById(taskStage.id!!).get()
            expectThat(taskStage.tasksResult.size).isEqualTo(1)
            expectThat(taskStage.tasksResult[0].code!!.characterStream.readText()).isEqualTo(xd2)
            val savedResults = taskStage.tasksResult[0].results!!.binaryStream.readAllBytes()
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
                "results" to result.results?.binaryStream?.let { String(it.readAllBytes()) },
                "code" to (result.code?.characterStream?.readText() ?: ""),
                "startTime" to result.startTime.toString(),
                "endTime" to result.endTime.toString(),
                "solverId" to result.taskStage.id!!.toString(),
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

    private fun send(msg: Map<String, String?>) = Json.encodeToString(msg).let {
        rabbitTemplate.send("task_result", Message(Base64.getEncoder().encode(it.toByteArray()), MessageProperties()))
    }
}
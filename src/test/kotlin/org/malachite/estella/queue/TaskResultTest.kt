package org.malachite.estella.queue

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.BeforeClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.BaseIntegration
import org.malachite.estella.aplication.domain.ApplicationRepository
import org.malachite.estella.aplication.domain.ApplicationStageRepository
import org.malachite.estella.commons.models.offers.Application
import org.malachite.estella.commons.models.offers.ApplicationStageData
import org.malachite.estella.commons.models.offers.ApplicationStatus
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.commons.models.tasks.TaskStage
import org.malachite.estella.interview.domain.InterviewRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.process.domain.RecruitmentStageRepository
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.task.domain.TaskRepository
import org.malachite.estella.task.domain.TaskStageRepository
import org.malachite.estella.util.DatabaseReset
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.event.annotation.BeforeTestClass
import org.springframework.transaction.annotation.Transactional
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.BufferedReader
import java.sql.Date
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

@DatabaseReset
class TaskResultTest : BaseIntegration() {
    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var taskRepository: TaskRepository

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
        val applicationStageData = ApplicationStageData(null, stage, savedApplication, null, null)
        val savedApplicationStageData = applicationStageDataRepository.save(applicationStageData)
        this.taskStage = TaskStage(null, setOf(), savedApplicationStageData)
        taskStage = taskStageRepository.save(taskStage)
        task = taskRepository.save(task)
    }

    @Test
    fun `task result consuming`() {
        expectThat(taskStage.tasksResult.size).isEqualTo(0)

        val taskResult = TaskResult(null, SerialBlob("xd".toByteArray()), SerialClob("xd".toCharArray()), null, null, task, taskStage)
        publish(taskResult)

        Thread.sleep(200)

        taskStage = taskStageRepository.findById(taskStage.id!!).get()
        expectThat(taskStage.tasksResult.size).isEqualTo(1)
    }


    fun publish(result: TaskResult) {
        try {
            val resultBody = mapOf(
                    "results" to String(result.results.binaryStream.readAllBytes()),
                    "code" to result.code.characterStream.readText(),
                    "startTime" to result.startTime.toString(),
                    "endTime" to result.endTime.toString(),
                    "solverId" to result.taskStage.id!!.toString(),
                    "taskId" to result.task.id!!.toString()
            )
            val msg = Json.encodeToString(resultBody)
            rabbitTemplate.send("task_result", Message(msg.toByteArray(), MessageProperties()))
        } catch (e: Exception) {
            println("Couldn't send test message to consumer")
            throw e
        }
    }
}
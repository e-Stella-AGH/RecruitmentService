package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.quizes.Quiz
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import java.sql.Date
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

data class RecruitmentProcessDto(
    val id: Int?,
    val startDate: Date,
    val endDate: Date?,
    val offer: OfferResponse,
    val stages: List<RecruitmentStage>,
    val quizzes: Set<Quiz>,
    val tasks: Set<TaskDto>
)
fun RecruitmentProcess.toRecruitmentProcessDto() = RecruitmentProcessDto(
    id,
    startDate,
    endDate,
    offer.toOfferResponse(),
    stages,
    quizzes,
    tasks.map { it.toTaskDto() }.toSet()
)

data class TaskDto(
    val id: Int?,
    val tests: String,
    val description: String,
    val timeLimit: Int,
    val deadline: Timestamp
) {
    companion object {
        fun toTask(task: TaskDto) = Task(task.id,
            SerialBlob(Base64.getDecoder().decode(task.tests)),
            SerialClob(String(Base64.getDecoder().decode(task.description)).toCharArray()),
            task.timeLimit,
            task.deadline)
    }
}
fun Task.toTaskDto() = TaskDto(
    id,
    Base64.getEncoder().encodeToString(
        this.tests.getBytes(1, this.tests.length().toInt())
    ),
    description.toString(),
    timeLimit,
    deadline
)

data class TaskTestDto(
    val testCaseId: Int?,
    val testData: String,
    val expectedResult: String
)
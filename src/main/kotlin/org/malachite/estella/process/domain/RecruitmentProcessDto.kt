package org.malachite.estella.process.domain

import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.quizes.Quiz
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import java.sql.Date
import java.sql.Timestamp

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
)
fun Task.toTaskDto() = TaskDto(
    id,
    tests.toString(),
    description.toString(),
    timeLimit,
    deadline
)
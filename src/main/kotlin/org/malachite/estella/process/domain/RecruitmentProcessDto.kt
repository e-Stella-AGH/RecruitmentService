package org.malachite.estella.process.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.toBase64String
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import java.sql.Date
import java.util.*


fun Set<RecruitmentStage>.getAsList(): List<RecruitmentStage> = this.toList().sortedBy { it.id }

data class RecruitmentProcessDto(
    val id: Int?,
    val startDate: Date?,
    val endDate: Date?,
    val offer: OfferResponse,
    val stages: List<RecruitmentStage>,
)

fun RecruitmentProcess.toRecruitmentProcessDto() = RecruitmentProcessDto(
    id,
    startDate,
    endDate,
    offer.toOfferResponse(),
    stages.getAsList()
)

data class TaskDto(
    val id: Int?,
    val testsBase64: String,
    val descriptionFileName: String,
    val descriptionBase64: String,
    val timeLimit: Int
)

fun TaskDto.toTask(): Task =
    Task(
        this.id,
        Base64.getDecoder().decode(this.testsBase64).toTypedArray(),
        this.descriptionBase64,
        this.descriptionFileName,
        this.timeLimit
    )

fun Task.toTaskDto() = TaskDto(
    id,
    this.tests.toBase64String(),
    this.descriptionFileName,
    this.description,
    timeLimit
)

@Serializable
data class TaskTestCaseDto(
    val testCaseId: Int?,
    val testData: String,
    val expectedResult: String
) {
    companion object {
        fun decodeFromJson(jsonBody: String): List<TaskTestCaseDto> =
            Json.decodeFromString(ListSerializer(serializer()), jsonBody)
    }
}

fun List<TaskTestCaseDto>.encodeToJson(): String =
    Json.encodeToString(ListSerializer(serializer()), this)
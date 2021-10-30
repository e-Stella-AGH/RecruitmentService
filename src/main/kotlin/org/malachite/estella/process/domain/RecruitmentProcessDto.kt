package org.malachite.estella.process.domain

import kotlinx.serialization.Serializable
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.commons.toBase64String
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.toOfferResponse
import java.sql.Date
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

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
    stages
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
        SerialBlob(Base64.getDecoder().decode(this.testsBase64)),
        SerialClob(String(Base64.getDecoder().decode(this.descriptionBase64)).toCharArray()),
        this.descriptionFileName,
        this.timeLimit
    )

fun Task.toTaskDto() = TaskDto(
    id,
    this.tests.toBase64String(),
    this.descriptionFileName,
    this.description.toBase64String(),
    timeLimit
)

@Serializable
data class TaskTestCaseDto(
    val testCaseId: Int?,
    val testData: String,
    val expectedResult: String
)
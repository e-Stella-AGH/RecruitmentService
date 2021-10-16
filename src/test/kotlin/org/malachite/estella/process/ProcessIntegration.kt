package org.malachite.estella.process

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.Message
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.malachite.estella.util.DatabaseReset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.*
import java.sql.Date
import java.time.Instant
import java.time.LocalDate

@DatabaseReset
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProcessIntegration: BaseIntegration() {

    @Autowired
    private lateinit var hrRepository: HrPartnerRepository

    @Test
    @Order(1)
    fun `should be able to update list of stages`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "HR_INTERVIEW", "HR_INTERVIEW", "TECHNICAL_INTERVIEW", "ENDED")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.OK)
        val newProcess = getProcesses().firstOrNull { it.id == process.id }
        expectThat(newProcess).isNotNull()
        expectThat(newProcess?.stages?.map { it.type }).isEqualTo(listOf(
            StageType.APPLIED,
            StageType.HR_INTERVIEW,
            StageType.HR_INTERVIEW,
            StageType.HR_INTERVIEW,
            StageType.TECHNICAL_INTERVIEW,
            StageType.ENDED
        ))
    }

    @Test
    @Order(2)
    fun `should be able to delete stage`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "TECHNICAL_INTERVIEW", "ENDED")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.OK)
        val newProcess = getProcesses().firstOrNull { it.id == process.id }
        expectThat(newProcess).isNotNull()
        expectThat(newProcess?.stages?.map { it.type }).isEqualTo(listOf(
            StageType.APPLIED,
            StageType.HR_INTERVIEW,
            StageType.TECHNICAL_INTERVIEW,
            StageType.ENDED
        ))
    }

    @Test
    @Order(3)
    fun `should throw exception, when first stage is not applied`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("HR_INTERVIEW", "TECHNICAL_INTERVIEW", "ENDED")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expect {
            val message = (updatedResponse.body as Map<String, Any>)["message"]
            that(message).isEqualTo("Stages list must start with APPLIED and end with ENDED")
        }
    }

    @Test
    @Order(4)
    fun `should throw exception, when last stage is not ended`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "TECHNICAL_INTERVIEW")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expect {
            val message = (updatedResponse.body as Map<String, Any>)["message"]
            that(message).isEqualTo("Stages list must start with APPLIED and end with ENDED")
        }
    }

    @Test
    @Order(5)
    fun `should throw exception, when there's no applied, nor ended in list`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("HR_INTERVIEW", "TECHNICAL_INTERVIEW")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expect {
            val message = (updatedResponse.body as Map<String, Any>)["message"]
            that(message).isEqualTo("Stages list must start with APPLIED and end with ENDED")
        }
    }

    @Test
    @Order(6)
    fun `should throw exception, when there's more than one applied`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "APPLIED", "TECHNICAL_INTERVIEW", "ENDED")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expect {
            val message = (updatedResponse.body as Map<String, Any>)["message"]
            that(message).isEqualTo("There must be only one APPLIED and ENDED stage")
        }
    }

    @Test
    @Order(6)
    fun `should throw exception, when there's more than one ended`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "ENDED", "TECHNICAL_INTERVIEW", "ENDED")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expect {
            val message = (updatedResponse.body as Map<String, Any>)["message"]
            that(message).isEqualTo("There must be only one APPLIED and ENDED stage")
        }
    }

    @Test
    @Order(7)
    fun `should be able to change end date of recruitment process`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val response = changeProcessEndDate("01.01.2022", process.id!!)
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val updatedProcess = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(updatedProcess).isNotNull()
        updatedProcess!!
        expectThat(updatedProcess.endDate?.toLocalDate()).isEqualTo(LocalDate.of(2022, 1, 1))
    }

    @Test
    @Order(8)
    fun `should throw exception when end date is going to be set before today, when start date isn't set`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        expectThat(process).isNotNull()
        process!!
        val response = changeProcessEndDate("01.01.1999", process.id!!)
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    @Order(9)
    fun `should be able to start process`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }
        process!!
        val response = startProcess(process.id!!)

        val now = Date.from(Instant.now())
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        expect {
            getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }?.startDate?.let {
                that(it.day).isEqualTo(now.day)
                that(it.month).isEqualTo(now.month)
                that(it.year).isEqualTo(now.year)
            }
        }
    }

    @Test
    @Order(10)
    fun `should not be able to modify process in any way after it was started`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == getHrPartnerMail() }

        process!!

        val errorMessage = "Process with id: ${process.id} has already been started, therefore it cannot be modified anymore!"

        changeProcessEndDate("01.01.2022", process.id!!).let {
            assertError(it, errorMessage)
        }

        listOf("APPLIED", "HR_INTERVIEW", "HR_INTERVIEW", "HR_INTERVIEW", "TECHNICAL_INTERVIEW", "ENDED").let {
            updateProcesses(process.id, it, process.offer.creator.user.mail)
        }.let {
            assertError(it, errorMessage)
        }

        startProcess(process.id!!).let {
            assertError(it, errorMessage)
        }
    }
    private fun assertError(response: Response, errorMessage: String) {
        expectThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        expectThat((response.body as Map<String, Any>)["message"]).isEqualTo(errorMessage)
    }

    private fun changeProcessEndDate(date: String, id: Int) =
        httpRequest(
            path = "/api/process/${id}/end_date",
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(getHrPartnerMail())),
            body = mapOf(
                "date" to date
            ),
            method = HttpMethod.PUT
        )

    private fun startProcess(processId: Int) = httpRequest(
        path = "/api/process/$processId/start",
        headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(getHrPartnerMail())),
        method = HttpMethod.PUT
    )


    private fun getHrPartnerMail() =
        hrRepository.findAll().toList()[1].user.mail

    private fun getProcesses(): List<RecruitmentProcessDto> {
        val response = httpRequest(
            path = "/api/process",
            method = HttpMethod.GET
        )
        response.body
            .let { it as List<Map<String, Any>> }
            .map { it.toRecruitmentProcessDto() }
            .let { return it }
    }

    private fun updateProcesses(id: Int?, stages: List<String>, mail: String): Response =
        httpRequest(
            path = "/api/process/stages/$id",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(mail)),
            body = mapOf("stages" to stages)
        )

    private fun loginUser(userMail: String): Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to "a"
            )
        )
    }

    private fun getAuthToken(mail: String):String =
        loginUser(mail).headers!![EStellaHeaders.authToken]!![0]
}
package org.malachite.estella.process

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProcessIntegration: BaseIntegration() {

    @Test
    @Order(1)
    fun `should be able to update list of stages`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == "alea@iacta.est" }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "HR_INTERVIEW", "HR_INTERVIEW", "TECHNICAL_INTERVIEW")
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
        ))
    }

    @Test
    @Order(2)
    fun `should be able to delete stage`() {
        val process = getProcesses().firstOrNull { it.offer.creator.user.mail == "alea@iacta.est" }
        expectThat(process).isNotNull()
        process!!
        val newStages = listOf("APPLIED", "HR_INTERVIEW", "TECHNICAL_INTERVIEW")
        val updatedResponse = updateProcesses(process.id, newStages, process.offer.creator.user.mail)
        expectThat(updatedResponse.statusCode).isEqualTo(HttpStatus.OK)
        val newProcess = getProcesses().firstOrNull { it.id == process.id }
        expectThat(newProcess).isNotNull()
        expectThat(newProcess?.stages?.map { it.type }).isEqualTo(listOf(
            StageType.APPLIED,
            StageType.HR_INTERVIEW,
            StageType.TECHNICAL_INTERVIEW,
        ))
    }

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
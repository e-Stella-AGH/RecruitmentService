package org.malachite.estella

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.malachite.estella.aplication.domain.ApplicationDTO
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.OrganizationResponse
import org.malachite.estella.people.domain.*
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.web.client.HttpStatusCodeException
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["mail_service_url=http://localhost:9797","admin_api_key=API_KEY","should_fake_load=false"]
)
class BaseIntegration {

    private val restTemplate = TestRestTemplate()
    val objectMapper = jacksonObjectMapper()

    private val file = Files.readAllBytes(Paths.get("src/main/kotlin/org/malachite/estella/stop-cv-format.pdf"))
    private val encodedFile: String = Base64.getEncoder().encodeToString(file)

    fun getJobSeekerFilePayload(fileName: String, id: Int? = null): JobSeekerFilePayload =
        JobSeekerFilePayload(id, fileName, encodedFile)

    fun httpRequest(
        path: String,
        method: HttpMethod,
        headers: Map<String, String> = mapOf(),
        body: Map<String, Any> = mapOf()
    ): Response {
        val httpHeaders = HttpHeaders().also {
            headers.forEach { (key, value) -> it.add(key, value) }
        }
        val uri = URI.create("http://localhost:8080$path")

        val requestEntity = RequestEntity(body, httpHeaders, method, uri)

        return try {
            val response = restTemplate.exchange(requestEntity, String::class.java)
            val statusCode = response.statusCode
            val responseBody = objectMapper.readValue(response.body, Any::class.java)
            Response(statusCode, responseBody, response.headers)
        } catch (exception: HttpStatusCodeException) {
            val responseBody = objectMapper.readValue(exception.responseBodyAsString, Any::class.java)
            val statusCode = exception.statusCode
            Response(statusCode, responseBody, exception.responseHeaders)
        }
    }

    @Test
    fun `test for httpRequest`() {
        //when http request is sent
        val response = httpRequest(
            path = "/_meta/health",
            method = HttpMethod.GET
        )
        response.body as Map<String, Any>

        //then response is what we want it to be
        expect {
            that(response.statusCode).isEqualTo(HttpStatus.OK)
            that(response.body).isEqualTo(mapOf("text" to "App works"))
        }
    }

    data class Response(
        val statusCode: HttpStatus,
        val body: Any,
        val headers: HttpHeaders?
    )

    fun Map<String, Any>.toUser() =
        User(
            this["id"] as Int?,
            this["firstName"] as String,
            this["lastName"] as String,
            this["mail"] as String,
            this["password"] as String?
        )

    fun Map<String, Any>.toOrganization() =
        Organization(
            UUID.fromString(this["id"] as String),
            this["name"] as String,
            (this["user"] as Map<String, Any>).toUser(),
            this["verified"] as Boolean
        )

    fun Map<String, Any>.toOrganizationResponse() =
        OrganizationResponse(
            this["name"] as String,
        )

    fun Map<String, Any>.toOfferResponse() =
        OfferResponse(
            this["id"] as Int,
            this["name"] as String,
            this["description"] as String,
            this["position"] as String,
            (this["minSalary"] as Int).toLong(),
            (this["maxSalary"] as Int).toLong(),
            this["localization"] as String,
            (this["organization"] as Map<String, Any>).toOrganizationResponse(),
            (this["skills"] as ArrayList<Map<String, Any>>).toDesiredSkillSet(),
            (this["creator"] as Map<String, Any>).toHrPartner()
        )

    fun ArrayList<Map<String, Any>>.toDesiredSkillSet() =
        this.map { it.toDesiredSkill() }
            .toHashSet()

    fun Map<String, Any>.toDesiredSkill() =
        DesiredSkill(
            this["id"] as Int,
            this["name"] as String,
            (this["level"] as String).toSkillLevel()!!
        )

    var simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")

    fun Map<String, Any>.toApplicationDTO() =
        ApplicationDTO(
            this["id"] as Int,
            Date.valueOf(this["applicationDate"] as String),
            ApplicationStatus.valueOf(this["status"] as String),
            (this["stage"] as Map<String, Any>).toRecruitmentStage(),
            (this["jobSeeker"] as Map<String, Any>).toJobSeekerDTO(),
            (this["seekerFiles"] as List<Map<String, Any>>).map { it.toJobSeekerFileDto() }.toSet()
        )

    fun String.toSkillLevel(): SkillLevel? {
        return when (this) {
            "NICE_TO_HAVE" -> SkillLevel.NICE_TO_HAVE
            "JUNIOR" -> SkillLevel.JUNIOR
            "REGULAR" -> SkillLevel.REGULAR
            "ADVANCED" -> SkillLevel.ADVANCED
            "MASTER" -> SkillLevel.MASTER
            else -> null
        }
    }

    fun Map<String, Any>.toHrPartner() = HrPartner(
        this["id"] as Int?,
        (this["organization"] as Map<String, Any>).toOrganization(),
        (this["user"] as Map<String, Any>).toUser()
    )

    fun Map<String, Any>.toJobSeeker() = JobSeeker(
        this["id"] as Int?,
        (this["user"] as Map<String, Any>).toUser(),
        mutableSetOf()
    )

    fun Map<String, Any>.toJobSeekerDTO() =
        JobSeekerDTO(this["id"] as Int, (this["user"] as Map<String, Any>).toUserDTO())

    fun Map<String, Any>.toHrPartnerResponse() =
        HrPartnerResponse(
            this["organizationName"] as String,
            (this["user"] as Map<String, Any>).toUserDTO()
        )

    fun Map<String, Any>.toUserDTO() =
        UserDTO(
            this["id"] as Int,
            this["firstName"] as String,
            this["lastName"] as String,
            this["mail"] as String
        )

    fun Map<String, Any>.toRecruitmentProcessDto() =
        RecruitmentProcessDto(
            this["id"] as Int?,
            Date.valueOf(this["startDate"] as String),
            (this["endDate"] as String?)?.let { Date.valueOf(it) },
            (this["offer"] as Map<String, Any>).toOfferResponse(),
            (this["stages"] as List<Map<String, Any>>).toRecruitmentStagesList(),
            setOf(),  //TODO - change it, when it will be implemented
            setOf()
        )

    fun Map<String, Any>.toJobSeekerFileDto() =
        JobSeekerFileDTO(
            this["id"] as Int,
            this["fileName"] as String,
            this["fileBase64"] as String
        )

    fun List<Map<String, Any>>.toRecruitmentStagesList() =
        this.map { it.toRecruitmentStage() }

    fun Map<String, Any>.toRecruitmentStage() = RecruitmentStage(
        this["id"] as Int?,
        StageType.valueOf(this["type"] as String)
    )

}
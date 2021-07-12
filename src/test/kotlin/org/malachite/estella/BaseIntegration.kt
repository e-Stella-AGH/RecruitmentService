package org.malachite.estella

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferResponse
import org.malachite.estella.offer.domain.OrganizationResponse
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
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["mail_service_url=http://localhost:9797","admin_api_key=API_KEY"]
)
class BaseIntegration {



    private val restTemplate = TestRestTemplate()
    val objectMapper = jacksonObjectMapper()

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
            (this["skills"] as ArrayList<Map<String, Any>>).toDesiredSkillSet()
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
}
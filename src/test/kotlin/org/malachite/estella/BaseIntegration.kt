package org.malachite.estella

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.web.client.HttpStatusCodeException
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
            headers.forEach{ (key, value) -> it.add(key, value) }
        }
        val uri = URI.create("http://localhost:8080$path")

        val requestEntity = RequestEntity(body, httpHeaders, method, uri)

        return try {
            val response = restTemplate.exchange(requestEntity, String::class.java)
            val statusCode = response.statusCode
            val responseBody = objectMapper.readValue(response.body, Any::class.java)
            Response(statusCode, responseBody)
        } catch (exception: HttpStatusCodeException) {
            val responseBody = objectMapper.readValue(exception.responseBodyAsString, Any::class.java)
            val statusCode = exception.statusCode
            Response(statusCode, responseBody)
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
        val body: Any
    )
}
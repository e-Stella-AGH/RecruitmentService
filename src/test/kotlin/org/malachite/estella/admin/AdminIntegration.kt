package org.malachite.estella.admin

import org.junit.jupiter.api.Test
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.util.DatabaseReset
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.TestDatabaseReseter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestExecutionListeners
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

@DatabaseReset
class AdminIntegration : BaseIntegration() {

    @Test
    fun `should verify organization`() {
        //given - stub for sending Emails
        EmailServiceStub.stubForSendEmail()

        //and - not verified organization
        val notVerifiedOrganization = getOrganizations().firstOrNull { it.verified == false }
        expectThat(notVerifiedOrganization).isNotNull()

        //when - sending http request to verify organization
        val response = httpRequest(
            path = "/_admin/verify/${notVerifiedOrganization!!.id}",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.adminApiKey to API_KEY)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        //then - check if organization was verified
        expect {
            val organization = getOrganizations().firstOrNull { it.id == notVerifiedOrganization.id }
            that(organization).isNotNull()
            that(organization!!.verified).isTrue()
        }
    }

    @Test
    fun `should deverify organization`() {
        //given - stub for sending emails
        EmailServiceStub.stubForSendEmail()

        //and - not verified organization
        val verifiedOrganization = getOrganizations().firstOrNull { it.verified == true }
        expectThat(verifiedOrganization).isNotNull()

        //when - sending http request to verify organization
        val response = httpRequest(
            path = "/_admin/deverify/${verifiedOrganization!!.id}",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.adminApiKey to API_KEY)
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        //then - check if organization was verified
        expect {
            val organization = getOrganizations().firstOrNull { it.id == verifiedOrganization.id }
            that(organization).isNotNull()
            that(organization!!.verified).isFalse()
        }
    }

    val API_KEY = "API_KEY"

    private fun getOrganizations() =
        httpRequest(
            path = "/api/organizations",
            method = HttpMethod.GET
        ).body
            .let {
                println(it)
                it as List<Map<String, Any>>
                it.map { it.toOrganization() }
            }

}
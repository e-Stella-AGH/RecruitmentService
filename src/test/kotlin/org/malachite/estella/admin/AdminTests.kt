package org.malachite.estella.admin

import org.junit.jupiter.api.Test
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.models.people.Organization
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.util.*

class AdminTests : BaseIntegration() {

    @Test
    fun `should verify organization`() {
        //given - not verified organization
        val notVerifiedOrganization = getOrganizations().firstOrNull { it.verified == false }
        expectThat(notVerifiedOrganization).isNotNull()

        //when - sending http request to verify organization
        val response = httpRequest(
            path="/_admin/verify/${notVerifiedOrganization!!.id}",
            method = HttpMethod.POST
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
        //given - not verified organization
        val verifiedOrganization = getOrganizations().firstOrNull { it.verified == true }
        expectThat(verifiedOrganization).isNotNull()

        //when - sending http request to verify organization
        val response = httpRequest(
            path="/_admin/deverify/${verifiedOrganization!!.id}",
            method = HttpMethod.POST
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)

        //then - check if organization was verified
        expect {
            val organization = getOrganizations().firstOrNull { it.id == verifiedOrganization.id }
            that(organization).isNotNull()
            that(organization!!.verified).isFalse()
        }
    }


    private fun getOrganizations() =
        httpRequest(
            path ="/api/organizations",
            method = HttpMethod.GET
        ).body
            .let {
                it as List<Map<String, Any>>
                it.map { toOrganization(it) }
            }

    private fun toOrganization(map: Map<String, Any>) =
        Organization(
            UUID.fromString(map["id"] as String),
            map["name"] as String,
            map["verified"] as Boolean
        )

}
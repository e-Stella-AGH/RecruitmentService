package org.malachite.estella.offer

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.malachite.estella.BaseIntegration
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.offer.domain.Skill
import org.malachite.estella.people.infrastrucutre.HibernateUserRepository
import org.malachite.estella.services.HrPartnerService
import org.malachite.estella.util.EmailServiceStub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class OffersIntegration: BaseIntegration() {

    @Autowired
    private lateinit var hrPartnerService: HrPartnerService
    @Autowired
    private lateinit var userRepository: HibernateUserRepository

    @Test
    @Order(1)
    fun `should be able to add offer`() {
        //given - organization and HrPartner
        EmailServiceStub.stubForSendEmail()

        val hrPartner = getHrPartner()
        setNewPassword("password123")

        val response = httpRequest(
            path = "/api/offers",
            method = HttpMethod.POST,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, "password123")),
            body = mapOf(
                "name" to name,
                "description" to description,
                "position" to position,
                "minSalary" to minSalary,
                "maxSalary" to maxSalary,
                "localization" to localization,
                "creatorId" to hrPartner.id!!,
                "skills" to emptyList<Skill>()
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val offers = getOffers()
        offers.find { it.name == name }.let {
            expectThat(it).isNotNull()
            it!!
            expect {
                that(it.description).isEqualTo(description)
                that(it.localization).isEqualTo(localization)
                that(it.maxSalary).isEqualTo(maxSalary)
                that(it.minSalary).isEqualTo(minSalary)
                that(it.position).isEqualTo(position)
                that(it.organization.name).isEqualTo(hrPartner.organization.name)
                that(it.skills).isEqualTo(emptySet())
            }
        }
    }

    @Test
    @Order(2)
    fun `should update offer`() {
        val offer = getOffers().let {
            it.find { it.name == name }
        }!!
        val hrPartner = getHrPartner()
        val response = httpRequest(
            path = "/api/offers/${offer.id}",
            method = HttpMethod.PUT,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, "password123")),
            body = mapOf(
                "name" to name,
                "description" to description,
                "position" to "New Position",
                "minSalary" to 10,
                "maxSalary" to 1000,
                "localization" to localization,
                "creatorId" to hrPartner.id!!,
                "skills" to emptyList<Skill>()
            )
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val offers = getOffers()
        offers.find { it.name == name }.let {
            expectThat(it).isNotNull()
            it!!
            expect {
                that(it.description).isEqualTo(description)
                that(it.localization).isEqualTo(localization)
                that(it.maxSalary).isEqualTo(1000)
                that(it.minSalary).isEqualTo(10)
                that(it.position).isEqualTo("New Position")
                that(it.organization.name).isEqualTo(hrPartner.organization.name)
                that(it.skills).isEqualTo(emptySet())
            }
        }
    }

    @Test
    @Order(3)
    fun `should delete offer`() {
        val offer = getOffers().let {
            it.find { it.name == name }
        }!!
        val hrPartner = getHrPartner()
        val response = httpRequest(
            path = "/api/offers/${offer.id}",
            method = HttpMethod.DELETE,
            headers = mapOf(EStellaHeaders.jwtToken to getAuthToken(hrPartner.user.mail, "password123"))
        )
        expectThat(response.statusCode).isEqualTo(HttpStatus.OK)
        println(getOffers())
        val deletedOffer = getOffers().firstOrNull { it.id == offer.id }
        expectThat(deletedOffer).isNull()
    }

    private fun setNewPassword(password: String) {
        getHrPartner().user.let {
            it.password = password
            userRepository.save(it)
        }
    }

    private fun getHrPartner() = hrPartnerService.getHrPartners().first()

    private fun getOffers() =
        httpRequest(
            path = "/api/offers",
            method = HttpMethod.GET
        ).also {
            expectThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }.body.let {
            it as List<Map<String, Any>>
            it.map { it.toOfferResponse() }
        }

    private fun loginUser(userMail: String, userPassword: String = password): Response {
        return httpRequest(
            path = "/api/users/login",
            method = HttpMethod.POST,
            body = mapOf(
                "mail" to userMail,
                "password" to userPassword
            )
        )
    }

    private fun getAuthToken(mail: String, password: String):String =
        loginUser(mail, password).headers!![EStellaHeaders.authToken]!![0]


    private val name = "Normal offer"
    private val organizationMail = "organization@hrpartner.pl"
    private val hrpartnerMail = "examplemail@hrpartner.pl"
    private val password = "123"

    private val description = "description"
    private val position = "position"
    private val minSalary = 1L
    private val maxSalary = 10L
    private val localization = "loc"
    private val newMaxSalary = 100L
    private val newPosition = "new position"

}
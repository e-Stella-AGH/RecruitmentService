package org.malachite.estella.offer

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.malachite.estella.offer.domain.OfferRequest
import org.malachite.estella.offer.domain.Skill
import org.malachite.estella.security.Authority
import org.malachite.estella.security.UserContextDetails
import org.malachite.estella.services.*
import org.malachite.estella.util.offersWithNullProcess
import org.malachite.estella.util.users
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class OfferServiceTest {

    private val repository = DummyOfferRepository()
    private val desiredSkillServiceMock = mockk<DesiredSkillService>()
    private val securityServiceMock = mockk<SecurityService>()
    private val applicationStageDataService = mockk<ApplicationStageDataService>()
    private val offerService = OfferService(repository, desiredSkillServiceMock, mockk(),applicationStageDataService, securityServiceMock)

    @BeforeEach
    fun setUp() {
        every { desiredSkillServiceMock.safeGetDesiredSkill(any()) } returns null
        every { desiredSkillServiceMock.addDesiredSkill(any()) } returns DesiredSkill(null, "xd", SkillLevel.JUNIOR)
        every { securityServiceMock.isCorrectApiKey(any()) } returns false
        every { securityServiceMock.isCorrectApiKey("abc") } returns false
    }

    @AfterEach
    fun cleanup() {
        repository.clear()
    }

    @Test
    fun `should add offer`() {
        //given
        val offer = offers[0]
        val offerId = offer.id!!
        //when
        offerService.addOffer(offer)
        //then
        expectThat(offerService.getOffer(offerId))
            .isEqualTo(offer)
    }

    @Test
    fun `should throw unauth exception when jwt is not from hr`() {
        val offer = offers[0]
        every { securityServiceMock.getHrPartnerFromContext() } returns null
        expectThrows<UnauthenticatedException> {
            offerService.addOffer(offer.toOfferRequest(1))
        }
    }

    @Test
    fun `should be able to update offer`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1, user = it.creator.user.copy(id = 1)), skills = setOf()) }
        offerService.addOffer(offer)

        val newOffer = offer.copy(name = "Totally new offer", localization = "Nope, it's not the same loc")
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
                offer.creator.user,
                "abc",
                setOf(Authority.hr),
                true
        )

        offerService.updateOffer(
            offer.id!!,
            OfferRequest(
                "Totally new offer",
                offer.description.characterStream.readText(),
                offer.position,
                offer.minSalary,
                offer.maxSalary,
                "Nope, it's not the same loc",
                1,
                offer.skills.toList().map { Skill(it.name, it.level.name) }
            )
        )

        expectThat(offerService.getOffer(offer.id!!))
            .isEqualTo(newOffer)
    }

    @Test
    fun `should throw unath when hr is not the same one who created`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1), skills = setOf()) }
        offerService.addOffer(offer)
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
                offer.creator.user.copy(1000),
                "abc",
                setOf(Authority.hr),
                true
        )

        expectThrows<UnauthenticatedException> {
            offerService.updateOffer(
                offer.id!!,
                OfferRequest(
                    "Totally new offer",
                    offer.description.characterStream.readText(),
                    offer.position,
                    offer.minSalary,
                    offer.maxSalary,
                    "Nope, it's not the same loc",
                    1,
                    offer.skills.toList().map { Skill(it.name, it.level.name) }
                )
            )
        }
    }

    @Test
    fun `organization should be able to update offer`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1)) }
        offerService.addOffer(offer)
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
                offer.creator.organization.user,
                "abc",
                setOf(Authority.organization),
                true
        )

        val newOffer = offer.copy(name = "Totally new offer", localization = "Nope, it's not the same loc")
        offerService.updateOffer(
            offer.id!!,
            OfferRequest(
                "Totally new offer",
                offer.description.characterStream.readText(),
                offer.position,
                offer.minSalary,
                offer.maxSalary,
                "Nope, it's not the same loc",
                offer.creator.id!!,
                offer.skills.toList().map { Skill(it.name, it.level.name) }
            )
        )

        expectThat(offerService.getOffer(offer.id!!))
            .isEqualTo(newOffer.copy(skills = setOf(DesiredSkill(id=null, name="xd", level=SkillLevel.JUNIOR))))
    }

    @Test
    fun `random user should not be able to update offer`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1)) }
        offerService.addOffer(offer)
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
                users[0],
                "abc",
                setOf(Authority.job_seeker),
                true
        )

        expectThrows<UnauthenticatedException> {
            offerService.updateOffer(offer.id!!, offer.toOfferRequest(1))
        }
    }


    @Test
    fun `should throw unath when hr is not the same one who created when deleting`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1, user = it.creator.user.copy(id = 1)), skills = setOf()) }
        offerService.addOffer(offer)
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
            offer.creator.user.copy(id = 100),
            null,
            setOf(Authority.hr),
            true
        )

        expectThrows<UnauthenticatedException> {
            offerService.deleteOffer(offer.id!!)
        }
    }

    @Test
    fun `random user should not be able to delete offer`() {
        val offer = offers[0].let { it.copy(creator = it.creator.copy(id = 1)) }
        offerService.addOffer(offer)
        every { securityServiceMock.getUserDetailsFromContext() } returns UserContextDetails(
                users[0],
                "abc",
                setOf(Authority.job_seeker),
                true
        )

        expectThrows<UnauthenticatedException> {
            offerService.deleteOffer(offer.id!!)
        }
    }

    @Test
    fun `should throw OfferNotFound exception when there's no such offer`() {
        expectThrows<OfferNotFoundException> {
            offerService.getOffer(0)
        }
    }

    private val offers = offersWithNullProcess

    private fun Offer.toOfferRequest(id: Int) = OfferRequest(
        this.name,
        this.description.characterStream.readText(),
        this.position,
        this.minSalary,
        this.maxSalary,
        this.localization,
        id,
        this.skills.toList().map { Skill(it.name, it.level.name) }
    )
}
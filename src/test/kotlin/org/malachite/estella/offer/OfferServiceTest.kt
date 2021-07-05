package org.malachite.estella.offer

import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.loader.FakeOffers
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.offer.domain.OfferNotFoundException
import org.malachite.estella.services.OfferService
import org.malachite.estella.util.offersWithNullProcess
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import java.util.*
import javax.sql.rowset.serial.SerialClob

class OfferServiceTest {

    private val repository = DummyOfferRepository()
    private val offerService = OfferService(repository, mockk(), mockk(), mockk())

    @AfterEach
    fun setup() {
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
    fun `should be able to update offer`() {
        val offer = offers[0]
        offerService.addOffer(offer)

        val newOffer = offer.copy(name = "Totally new offer", localization = "Nope, it's not the same loc")
        offerService.updateOffer(offer.id!!, newOffer)

        println(offer)
        println(newOffer)

        expectThat(offerService.getOffer(offer.id!!))
            .isEqualTo(newOffer)
    }

    @Test
    fun `should be able to delete offer`() {
        val offer = offers[0]
        offerService.addOffer(offer)

        offerService.deleteOffer(offer.id!!)
        expectThat(repository.size()).isEqualTo(0)
    }

    @Test
    fun `should throw OfferNotFound exception when there's no such offer`() {
        expectThrows<OfferNotFoundException> {
            offerService.getOffer(0)
        }
    }

    private val offers = offersWithNullProcess
}
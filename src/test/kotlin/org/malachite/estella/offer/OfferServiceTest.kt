package org.malachite.estella.offer

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.loader.FakeOffers
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.OfferService
import org.malachite.estella.util.offersWithNullProcess
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*
import javax.sql.rowset.serial.SerialClob

class OfferServiceTest {

    private val repository = DummyOfferRepository()
    private val offerService = OfferService(repository)

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

    private val offers = offersWithNullProcess
}
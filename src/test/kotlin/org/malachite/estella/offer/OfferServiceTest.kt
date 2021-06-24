package org.malachite.estella.offer

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.OfferService
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
        //when
        offerService.addOffer(offer)
        //then
        expectThat(offerService.getOffer(offerId))
            .isEqualTo(offer)
    }

    private val offerId = 1
    private val organizationUUID = UUID.randomUUID()
    private val user = User(null, "fname", "lname", "mail", "password")
    private val offer = Offer(offerId, "name", SerialClob("descirption".toCharArray()),
        "position", 0L, 0L, "loc",
        HrPartner(null, Organization(organizationUUID, "name", true), user),
        setOf(DesiredSkill(null, "name", SkillLevel.ADVANCED)),
        null
    )
}
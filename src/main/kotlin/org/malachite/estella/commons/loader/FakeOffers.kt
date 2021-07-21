package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import java.sql.Clob
import java.sql.Date
import java.util.*
import javax.sql.rowset.serial.SerialClob

object FakeOffers {

    data class OfferPayload(
        val name: String, val description: Clob, val position: String,
        val minSalary: Long, val maxSalary: Long, val localization: String,
        val skills: Set<DesiredSkill>
    ) {
        fun toOffer(hrPartner: HrPartner): Offer {
            return Offer(
                null, name, description, position, minSalary,
                maxSalary, localization, hrPartner, skills, null
            )
        }
    }

    fun getOffers(hrPartners: List<HrPartner>, desiredSkills: List<DesiredSkill>): List<Offer> {
        val payloads = listOf<OfferPayload>(
            OfferPayload(
                "Software Engineer Intern", SerialClob("The best offer of your live".toCharArray()),
                "Software Engineer Intern", 3000, 5500, "Cracow, Long street 5",
                desiredSkills.subList(0, 5).toSet()
            ),
            OfferPayload(
                "Senior developer Java", SerialClob("Master Java developer".toCharArray()),
                "Senior developer Java", 25000, 35500, "Warsaw, Wiejska 3",
                desiredSkills.subList(3, 7).toSet()
            ),
            OfferPayload(
                "Project Manager", SerialClob("Be manager of your life".toCharArray()),
                "Senior developer Java", 15000, 25500, "Paris, Republica 1",
                desiredSkills.subList(2, 3).toSet()
            ),
            OfferPayload(
                "Slave", SerialClob("Your freedom end now".toCharArray()),
                "Slave", 1, 1, "Virgina, Jefferson Plantation ",
                setOf()
            ),
            OfferPayload(
                "Centurion", SerialClob("Join IX Legion. Join conquering the Great Britain".toCharArray()),
                "Centurion", 10000, 20000, "Londonium, via Rome  XIV",
                desiredSkills.subList(7, 9).toSet()
            )
        )

        return payloads.mapIndexed { index, offerPayload ->
            offerPayload.toOffer(hrPartners[index % hrPartners.size])
        }
    }

}
package org.malachite.estella.offer.domain

import org.malachite.estella.commons.models.offers.DesiredSkill
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.SkillLevel
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.services.DesiredSkillService
import org.malachite.estella.services.HrPartnerService
import java.util.HashSet
import javax.sql.rowset.serial.SerialClob

data class OfferRequest(
    val name: String, val description: String, val position: String,
    val minSalary: Long, val maxSalary: Long, val localization: String,
    val creatorId: Int, val skills: List<Skill>
) {

    fun toOffer(hrPartner: HrPartner, desiredSkillService: DesiredSkillService): Offer {

        val skillSet = toDesiredSkillSet(desiredSkillService)
            .map { if (it.id != null) it else desiredSkillService.addDesiredSkill(it) }.toSet()

        return Offer(
            null, name,
            SerialClob(description.toCharArray()), position, minSalary, maxSalary,
            localization, hrPartner, skillSet, null
        )
    }

    private fun toDesiredSkillSet(desiredSkillService: DesiredSkillService): HashSet<DesiredSkill> = skills
        .map {
            desiredSkillService
                .safeGetDesiredSkill(Pair(it.name, SkillLevel.valueOf(it.level))) ?: it.toDesiredSkill()
        }
        .toCollection(HashSet<DesiredSkill>())
}

data class Skill(
    val name: String,
    val level: String
) {
    fun toDesiredSkill(): DesiredSkill = DesiredSkill(null, name, SkillLevel.valueOf(level))
}

data class OfferResponse(
    val id: Int?, val name: String, val description: String, val position: String,
    val minSalary: Long, val maxSalary: Long, val localization: String,
    val organization: OrganizationResponse, val skills: Set<DesiredSkill>
) {
    companion object {
        fun fromOffer(offer: Offer): OfferResponse {
            return OfferResponse(
                offer.id,
                offer.name,
                offer.description.characterStream.readText(),
                offer.position,
                offer.minSalary,
                offer.maxSalary,
                offer.localization,
                offer.getOrganizationResponse(),
                offer.skills
            )
        }
    }
}

data class OrganizationResponse(
    val name: String
)

fun Offer.getOrganizationResponse() = OrganizationResponse(
    this.creator.organization.name
)
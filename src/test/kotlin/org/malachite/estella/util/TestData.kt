package org.malachite.estella.util

import org.malachite.estella.commons.loader.FakeDesiredSkills
import org.malachite.estella.commons.loader.FakeLoader
import org.malachite.estella.commons.loader.FakeOffers
import org.malachite.estella.commons.loader.FakeOrganizations
import java.util.*

val desiredSkills = FakeDesiredSkills.desiredSkills
    .mapIndexed { index, desiredSkill ->
        desiredSkill.copy(id = index)
    }

val hrPartners = FakeLoader.getHrPartners(
    FakeOrganizations.companies
        .mapIndexed { index, company ->
            company.copy(UUID.randomUUID())
        }
)

val offersWithNullProcess = FakeOffers.getOffers(hrPartners, desiredSkills)
    .mapIndexed { index, offer ->
        offer.copy(id = index)
    }
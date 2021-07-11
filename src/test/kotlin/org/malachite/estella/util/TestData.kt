package org.malachite.estella.util

import org.malachite.estella.commons.loader.*
import java.util.*

val desiredSkills = FakeDesiredSkills.desiredSkills
    .mapIndexed { index, desiredSkill ->
        desiredSkill.copy(id = index)
    }

val hrPartners = FakeLoader.getHrPartners(
    FakeOrganizations.getCompanies(FakeUsers.organizationUsers)
        .mapIndexed { index, company ->
            company.copy(UUID.randomUUID())
        }
)

val offersWithNullProcess = FakeOffers.getOffers(hrPartners, desiredSkills)
    .mapIndexed { index, offer ->
        offer.copy(id = index)
    }

val users = FakeUsers.users.mapIndexed { index, user -> user.copy(id = index) }
val jobSeekers = FakeLoader.getFakeJobSeekers()
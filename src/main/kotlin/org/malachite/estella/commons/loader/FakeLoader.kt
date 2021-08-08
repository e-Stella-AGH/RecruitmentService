package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import java.util.*


object FakeLoader {
    fun getHrPartners(companies: List<Organization>) =
        FakeUsers.users
            .filterIndexed { index, _ -> index % 2 == 0 }
            .mapIndexed { index, user ->
                HrPartner(
                    id = null,
                    user = user,
                    organization = companies[index % companies.size]
                )
            }

    fun getFakeJobSeekers() =
        FakeUsers.users
            .filterIndexed { index, _ -> index % 2 != 0 }
            .map {
                JobSeeker(
                    id = null,
                    user = it,
                    files = Collections.emptySet()
                )
            }
}
package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User

object FakeOrganizations {


    fun getCompanies(organizationsUsers: List<User>) = listOf<Organization>(
        Organization(
            id = null,
            name = "Qualtrics",
            user = organizationsUsers[0],
            verified = true
        ),
        Organization(
            id = null,
            name = "Pega",
            user = organizationsUsers[1],
            verified = true,
        )
    )
}
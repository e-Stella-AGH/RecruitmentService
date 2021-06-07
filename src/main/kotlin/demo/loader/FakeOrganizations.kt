package demo.loader

import demo.models.people.Organization

object FakeOrganizations {
    val companies = listOf<Organization>(
        Organization(
            id = null,
            name = "Qualtrics",
            verified = true
        ),
        Organization(
            id = null,
            name = "Pega",
            verified = true
        )
    )
}
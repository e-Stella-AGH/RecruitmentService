package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.people.User

object FakeUsers {
    val users: List<User> = listOf(
        User(
            id = null,
            firstName = "Octavian",
            lastName = "Augustus",
            mail = "principus@roma.com",
            password = "a"
        ),
        User(
            id = null,
            firstName = "Marcus",
            lastName = "Cato",
            mail = "carthago@delenda.est",
            password = "a"
        ),
        User(
            id = null,
            firstName = "Gaius",
            lastName = "Caesar",
            mail = "alea@iacta.est",
            password = "a"
        ),
        User(
            id = null,
            firstName = "Titus",
            lastName = "Vespasianus",
            mail = "pecunia@non.olet",
            password = "a"
        )
    )

    val organizationUsers = listOf(
        User(null,"Qualtrics","","hr@qualtrics.com","a"),
        User(null,"Pega","","hr@pega.com","a")
    )
}




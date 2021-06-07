package demo.loader

import demo.models.offers.DesiredSkill
import demo.models.offers.Offer
import demo.models.offers.SkillLevel
import demo.models.people.HrPartner
import demo.models.people.Organization
import demo.models.people.User
import java.sql.Clob
import javax.sql.rowset.serial.SerialClob

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
}




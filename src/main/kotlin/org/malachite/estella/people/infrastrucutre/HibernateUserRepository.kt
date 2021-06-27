package org.malachite.estella.people.infrastrucutre

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HibernateUserRepository : CrudRepository<User, Int>, UserRepository {
    override fun findByMail(mail: String): Optional<User>
}
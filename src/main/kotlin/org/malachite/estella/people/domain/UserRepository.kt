package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.User
import java.util.*

interface UserRepository {
    fun findAll(): MutableIterable<User>
    fun findByMail(mail:String): Optional<User>
    fun findById(id: Int): Optional<User>
    fun save(user: User): User
    fun deleteById(id: Int)
}
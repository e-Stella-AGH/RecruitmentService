package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.User
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface UserRepository {
    fun findAll(): MutableIterable<User>
    fun findByMail(mail:String): Optional<User>
    fun findById(id: Int): Optional<User>
    fun save(user: User): User
    fun deleteById(id: Int): User
}
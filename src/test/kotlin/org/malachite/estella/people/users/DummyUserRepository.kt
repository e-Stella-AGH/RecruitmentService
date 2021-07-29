package org.malachite.estella.people.users

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserRepository
import java.util.*

class DummyUserRepository: UserRepository {

    private val users: MutableList<User> = mutableListOf()

    override fun findAll(): MutableIterable<User> = users

    override fun findByMail(mail: String): Optional<User> = Optional.ofNullable(users.firstOrNull { it.mail == mail })

    override fun findById(id: Int): Optional<User> = Optional.ofNullable(users.firstOrNull { it.id == id })

    override fun save(user: User): User {
        if(user.id == null) return user.copy(id = this.users.size).also { users.add(it) }
        if(user.id!! < users.size) {
            users[user.id!!] = user
            return user
        }
        users.add(user)
        return user
    }

    override fun deleteById(id: Int): User =
        findById(id).get()
            .also { users.remove(it) }

    fun size(): Int = users.size

    fun clear() {
        users.clear()
    }
}
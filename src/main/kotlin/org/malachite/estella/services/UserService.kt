package org.malachite.estella.services

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserRepository
import org.malachite.estella.people.infrastrucutre.HibernateUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(@Autowired private val userRepository: UserRepository) {

    fun getUsers(): MutableIterable<User> =
        userRepository.findAll()

    fun getUser(id: Int): User =
        userRepository.findById(id).get()

    fun addUser(user: User): User = userRepository.save(user)

    fun updateUser(id: Int, user: User) {
        val currUser: User = getUser(id)
        val updated: User = currUser.copy(
            firstName = user.firstName,
            lastName = user.lastName, mail = user.mail
        )
        updated.password = user.password
    }

    fun deleteUser(id: Int) = userRepository.deleteById(id)

    fun getUserByEmail(email: String): User? = userRepository.findByMail(email).orElse(null)


}
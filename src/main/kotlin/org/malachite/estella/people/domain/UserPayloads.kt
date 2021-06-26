package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.User

data class UserDTO(val id: Int,
                   val firstName: String,
                   val lastName: String,
                   val mail: String) {
    companion object {
        fun fromUser(user: User) = UserDTO(
                user.id!!,
                user.firstName,
                user.lastName,
                user.mail
        )
    }
}
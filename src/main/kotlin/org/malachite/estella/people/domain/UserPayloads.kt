package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.User

data class UserDTO(val id: Int,
                   val firstName: String,
                   val lastName: String,
                   val mail: String)

fun User.toUserDTO():UserDTO =
    UserDTO(
        this.id!!,
        this.firstName,
        this.lastName,
        this.mail
    )
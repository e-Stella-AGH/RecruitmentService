package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.User
import org.malachite.estella.services.SecurityService

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

data class UserRequest(
    val firstName: String,
    val lastName: String,
    val mail: String,
    val password: String
) {
    fun toUser() = User(null, firstName, lastName, mail, password)
}

data class LoginRequest(val mail: String, val password: String)

data class UserResponse(
    val firstName: String,
    val lastName: String,
    val mail: String,
    val userType: String
)
fun User.toUserResponse(securityService: SecurityService, jwt: String?) =
    UserResponse(
        this.firstName,
        this.lastName,
        this.mail,
        securityService.getUserTypeByJWT(jwt)
    )

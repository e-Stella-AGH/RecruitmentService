package demo.dto.user

import demo.models.people.User

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
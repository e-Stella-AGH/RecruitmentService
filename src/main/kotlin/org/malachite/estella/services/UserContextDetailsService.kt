package org.malachite.estella.services

import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.security.UserContextDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserContextDetailsService(
        @Autowired val userService: UserService
): UserDetailsService{
    override fun loadUserByUsername(username: String?): UserDetails =
        username?.let {
            val user = userService.getUserByEmail(it)
            UserContextDetails(
                    user!!,
                    null,
                    userService.getUserType(user.id!!)?.let { type -> setOf(type) } ?: setOf(),
                    true
            )
        } ?: throw UnauthenticatedException()
}
package org.malachite.estella.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserContextDetailsService(
        @Autowired val userService: UserService
): UserDetailsService{
    override fun loadUserByUsername(username: String?): UserDetails =
            userService.getUserContextDetails(
                    userService.getUserByEmail(username ?: "")!!
            )
}
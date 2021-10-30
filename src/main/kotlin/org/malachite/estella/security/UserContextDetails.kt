package org.malachite.estella.security

import org.malachite.estella.commons.models.people.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

class UserContextDetails(
    val user: User,
    val token: String?,
    val _authorities: Collection<Authority>,
    val _enabled: Boolean
) : UserDetails {
    override fun getAuthorities(): Collection<Authority> = _authorities
    override fun getPassword(): String = user.password
    override fun getUsername(): String = user.mail
    override fun isAccountNonExpired(): Boolean = _enabled
    override fun isAccountNonLocked(): Boolean = _enabled
    override fun isCredentialsNonExpired(): Boolean = _enabled
    override fun isEnabled(): Boolean = _enabled
    override fun toString(): String = user.mail

    companion object {
        fun fromContext(): UserContextDetails? = SecurityContextHolder
            .getContext()
            ?.authentication
            ?.principal
            ?.let {
                if (it is UserContextDetails) it
                else null
            }
    }
}
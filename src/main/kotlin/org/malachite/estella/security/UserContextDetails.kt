package org.malachite.estella.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

class UserContextDetails(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val mail: String,
    val _authorities: Collection<Authority>,
    val _enabled: Boolean
) : UserDetails {
    override fun getAuthorities(): Collection<Authority> = _authorities
    override fun getPassword(): String = ""
    override fun getUsername(): String = mail
    override fun isAccountNonExpired(): Boolean = _enabled
    override fun isAccountNonLocked(): Boolean = _enabled
    override fun isCredentialsNonExpired(): Boolean = _enabled
    override fun isEnabled(): Boolean = _enabled
    override fun toString(): String = mail

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
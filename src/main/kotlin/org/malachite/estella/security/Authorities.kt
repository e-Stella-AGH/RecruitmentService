package org.malachite.estella.security

import org.springframework.security.core.GrantedAuthority

enum class Authority : GrantedAuthority {
    job_seeker, hr, organization;

    override fun getAuthority(): String = this.name
}
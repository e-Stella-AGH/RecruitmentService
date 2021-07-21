package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.HrPartner

data class HrPartnerResponse(val organizationName: String, val user: UserDTO) {
}

fun HrPartner.toResponse() = HrPartnerResponse(
    this.organization.name,
    this.user.toUserDTO()
)

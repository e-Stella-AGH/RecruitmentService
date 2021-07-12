package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.HrPartner

data class HrPartnerResponse(val organizationName: String, val user: UserDTO) {
    companion object {
        fun fromHrPartner(hrPartner: HrPartner) = HrPartnerResponse(
            hrPartner.organization.name,
            UserDTO.fromUser(hrPartner.user)
        )
    }
}

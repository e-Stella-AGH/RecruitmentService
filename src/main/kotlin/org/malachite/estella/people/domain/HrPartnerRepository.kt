package org.malachite.estella.people.domain

import org.malachite.estella.commons.models.people.HrPartner
import java.util.*

interface HrPartnerRepository {
    fun findAll(): MutableIterable<HrPartner>
    fun findById(id: Int): Optional<HrPartner>
    fun save(hrPartner: HrPartner): HrPartner
    fun deleteById(id: Int): Optional<HrPartner>
}
package org.malachite.estella.services

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HrPartnerService(@Autowired private val hrPartnerRepository: HrPartnerRepository) {
    fun getHrPartners(): MutableIterable<HrPartner> = hrPartnerRepository.findAll()

    fun getHrPartner(id: Int): HrPartner = hrPartnerRepository.findById(id).get()

    fun addHrPartner(hrPartner: HrPartner): HrPartner = hrPartnerRepository.save(hrPartner)

    fun updateHrPartner(id: Int, hrPartner: HrPartner) {
        val currPartner: HrPartner = hrPartnerRepository.findById(id).get()
        val updated: HrPartner = currPartner.copy(organization = hrPartner.organization,
                user = hrPartner.user)

        hrPartnerRepository.save(updated)

    }

    fun deleteHrPartner(id: Int) = hrPartnerRepository.deleteById(id)
}
package demo.services

import demo.models.people.HrPartner
import demo.repositories.HrPartnerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

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

package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.UserNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HrPartnerService(
    @Autowired private val hrPartnerRepository: HrPartnerRepository,
    @Autowired private val mailService: MailService,
    @Autowired private val userService: UserService
): EStellaService() {

    override val throwable: Exception = UserNotFoundException()

    fun getHrPartners(): MutableIterable<HrPartner> = hrPartnerRepository.findAll()

    fun getHrPartner(id: Int): HrPartner = withExceptionThrower { hrPartnerRepository.findByUserId(id).get() } as HrPartner

    fun addHrPartner(hrPartner: HrPartner): HrPartner = hrPartnerRepository.save(hrPartner)

    fun registerHrPartner(hrPartner: HrPartner): HrPartner {
        val password = userService.generatePassword()
        hrPartner.user.password = password
        val user = userService.addUser(hrPartner.user)
        val resultHrPartner = addHrPartner(hrPartner.copy(user = user))
        mailService.sendHrPartnerRegisterMail(resultHrPartner, password)
        return hrPartner
    }

    fun updateHrPartner(id: Int, hrPartner: HrPartner) {
        val currPartner: HrPartner = this.getHrPartner(id)
        val updated: HrPartner = currPartner.copy(
            organization = hrPartner.organization,
            user = hrPartner.user
        )
        hrPartnerRepository.save(updated)
    }

    fun deleteHrPartner(id: Int) = hrPartnerRepository.deleteById(id)
}

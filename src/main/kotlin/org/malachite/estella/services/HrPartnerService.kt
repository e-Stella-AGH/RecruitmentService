package org.malachite.estella.services

import org.malachite.estella.commons.DataViolationException
import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.Permission
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.api.HrPartnerRequest
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.UserNotFoundException
import org.malachite.estella.security.Authority
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HrPartnerService(
        @Autowired private val hrPartnerRepository: HrPartnerRepository,
        @Autowired private val organizationRepository: OrganizationRepository,
        @Autowired private val mailService: MailService,
        @Autowired private val userService: UserService,
        @Autowired private val securityService: SecurityService
): EStellaService<HrPartner>() {

    override val throwable: Exception = UserNotFoundException()

    fun getHrPartners(): MutableIterable<HrPartner> = hrPartnerRepository.findAll()

    fun getHrPartner(id: Int): HrPartner = withExceptionThrower { hrPartnerRepository.findByUserId(id).get() }

    private fun addHrPartner(hrPartner: HrPartner): HrPartner = hrPartnerRepository.save(hrPartner)

    fun registerHrPartner(hrPartnerRequest: HrPartnerRequest): HrPartner {
        getPermissions(null).let {
            if (!it.contains(Permission.CREATE)) throw UnauthenticatedException()
        }
        val organization = securityService.getOrganizationFromContext() ?: throw UnauthenticatedException()
        val hrPartner = hrPartnerRequest.toHrPartner(organization)
        return registerHrPartner(hrPartner)
    }
    fun getHrPartnerByMail(mail: String?): HrPartner? = getHrPartners().find { it.user.mail == mail }

    fun registerHrPartner(hrPartner: HrPartner): HrPartner {
        val password = userService.generatePassword()
        hrPartner.user.password = password
        val user = userService.addUser(hrPartner.user)
        val resultHrPartner = addHrPartner(hrPartner.copy(user = user))
        mailService.sendHrPartnerRegisterMail(resultHrPartner, password)
        return hrPartner
    }

    private fun updateHrPartner(id: Int, hrPartner: HrPartner) {
        if(!getPermissions(id).contains(Permission.UPDATE)) throw UnauthenticatedException()
        val currPartner: HrPartner = this.getHrPartner(id)
        val updated: HrPartner = currPartner.copy(
            organization = hrPartner.organization,
            user = hrPartner.user
        )
        hrPartnerRepository.save(updated)
    }

    fun getHrsByOrganizationUserId(userId: Int): Collection<HrPartner> =
        organizationRepository
                .findByUserId(userId)
                .orElse(null)
                ?.let {
                    this.getHrPartners()
                        .filter { it.organization.user.id == userId }
                } ?: listOf()

    private fun getHrsIdsByOrganizationUserId(userId: Int) =
        this.getHrsByOrganizationUserId(userId)
            .map { it.id }

    fun getPermissions(id: Int?): Set<Permission> {
        val userDetails = securityService.getUserDetailsFromContext()
        if (securityService.isCorrectApiKey(userDetails?.token))
            return Permission.allPermissions()

        val user = userDetails?.user ?: throw UnauthenticatedException()
        val userAuthority = userDetails.authorities.firstOrNull() ?: throw UnauthenticatedException()
        if (userAuthority == Authority.hr && user.id == id) {
            return Permission.allPermissions()
        }
        if (
                userAuthority == Authority.organization
                && getHrsIdsByOrganizationUserId(user.id!!).contains(id)
                && organizationRepository.findByUserId(user.id).filter { it.verified }.isPresent
        ) {
            return Permission.allPermissions()
        }
        if (userAuthority == Authority.organization && id == null) {
            return setOf(Permission.READ, Permission.CREATE)
        }
        return setOf(Permission.READ)
    }

    fun deleteHrPartner(id: Int) = try {
        if(!getPermissions(id).contains(Permission.DELETE)) throw UnauthenticatedException()
        hrPartnerRepository.deleteById(id)
    } catch (ex: Exception) {
        throw DataViolationException("This user may have offers assigned to him and cannot be deleted")
    }

    fun deleteHrPartnerByMail(mail: String) {
        this.getHrPartnerByMail(mail)?.let {
            if (!securityService.checkOrganizationHrRights(it.user.id!!)) throw UnauthenticatedException()
            this.deleteHrPartner(it.id!!)
        } ?: throw UserNotFoundException()
    }
}

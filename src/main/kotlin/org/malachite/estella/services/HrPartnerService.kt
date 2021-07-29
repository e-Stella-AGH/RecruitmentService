package org.malachite.estella.services

import org.malachite.estella.commons.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.people.api.HrPartnerRequest
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.UserNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class HrPartnerService(
    @Autowired private val hrPartnerRepository: HrPartnerRepository,
    @Autowired private val mailService: MailService,
    @Autowired private val userService: UserService,
    @Autowired private val securityService: SecurityService
): EStellaService<HrPartner>() {

    override val throwable: Exception = UserNotFoundException()

    fun getHrPartners(): MutableIterable<HrPartner> = hrPartnerRepository.findAll()

    fun getHrPartner(id: Int): HrPartner = withExceptionThrower { hrPartnerRepository.findByUserId(id).get() }

    private fun addHrPartner(hrPartner: HrPartner): HrPartner = hrPartnerRepository.save(hrPartner)

    fun registerHrPartner(hrPartnerRequest: HrPartnerRequest, jwt: String?): HrPartner {
        getPermissions(null, jwt).let {
            if (!it.contains(Permission.CREATE)) throw UnauthenticatedException()
        }
        val organization = securityService.getOrganizationFromJWT(jwt) ?: throw UnauthenticatedException()
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

    fun updateHrPartner(id: Int, hrPartner: HrPartner, jwt: String?) {
        if(!getPermissions(id, jwt).contains(Permission.UPDATE)) throw UnauthenticatedException()
        updateHrPartner(id, hrPartner)
    }

    private fun updateHrPartner(id: Int, hrPartner: HrPartner) {
        val currPartner: HrPartner = this.getHrPartner(id)
        val updated: HrPartner = currPartner.copy(
            organization = hrPartner.organization,
            user = hrPartner.user
        )
        hrPartnerRepository.save(updated)
    }

    fun getHrsByOrganizationId(id: UUID?) =
        this.getHrPartners()
            .filter { it.organization.id == id }

    private fun getHrsIdsByOrganizationId(id: UUID?) =
        this.getHrsByOrganizationId(id)
            .map { it.id }

    fun deleteHrPartner(id: Int, jwt: String?) {
        if(!getPermissions(id, jwt).contains(Permission.DELETE)) throw UnauthenticatedException()
        deleteHrPartner(id)
    }

    fun getPermissions(id: Int?, jwt: String?): Set<Permission> {
        if (securityService.isCorrectApiKey(jwt)) return Permission.allPermissions()
        val permissions = mutableSetOf(Permission.READ)
        val user = securityService.getHrPartnerFromJWT(jwt) ?: securityService.getOrganizationFromJWT(jwt)
        ?: throw UnauthenticatedException()
        if (user is HrPartner && user.id == id) {
            permissions.addAll(Permission.allPermissions())
        }
        if (user is Organization && getHrsIdsByOrganizationId(user.id).contains(id) && user.verified) {
            permissions.addAll(Permission.allPermissions())
        }
        if (user is Organization && id == null) {
            permissions.add(Permission.CREATE)
        }
        return permissions
    }

    fun deleteHrPartner(id: Int) = try {
        hrPartnerRepository.deleteById(id)
    } catch (ex: Exception) {
        throw DataViolationException("This user may have offers assigned to him and cannot be deleted")
    }

    fun deleteHrPartnerByMail(organizationJwt: String?, mail: String) {
        this.getHrPartnerByMail(mail)?.let {
            if (!securityService.checkOrganizationHrRights(organizationJwt, it.user.id!!)) throw UnauthenticatedException()
            this.deleteHrPartner(it.id!!)
        } ?: throw UserNotFoundException()
    }
}

package org.malachite.estella.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import java.util.*
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.InvalidUserException
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.people.domain.UserRepository
import org.malachite.estella.security.UserContextDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8


@Service
class SecurityService(
    @Autowired val userRepository: UserRepository,
    @Autowired val jobSeekerRepository: JobSeekerRepository,
    @Autowired val hrPartnerRepository: HrPartnerRepository,
    @Autowired val organizationRepository: OrganizationRepository,
    @Value("\${admin_api_key}") final val API_KEY: String
) {

    private val authSecret = "secret"
    private val refreshSecret = "refreshSecret"
    private val refreshTime = 3600 * 1000 * 24 // 1 day
    private val authTime = 15 * 60 * 1000 // 15 minutes

    private val mailKey = "mail"
    private val firstNameKey = "firstName"
    private val lastNameKey = "lastName"
    private val userTypeKey = "userType"
    private val algorithm = "SHA-256"


    private fun sha256(str: String): String = MessageDigest
        .getInstance(algorithm)
        .digest(str.toByteArray(UTF_8))
        .toHex()

    fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    fun hashOrganization(organization: Organization) =
        sha256("${organization.id}:${organization.user.password}")

    fun compareOrganizationWithPassword(organization: Organization, password: String) =
        hashOrganization(organization).equals(password)

    private fun getAuthenticateToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + authTime))
            .claim(mailKey, user.mail)
            .claim(firstNameKey, user.firstName)
            .claim(lastNameKey, user.lastName)
            .claim(userTypeKey, user.getUserType())
            .signWith(SignatureAlgorithm.HS512, authSecret)
            .compact()
    }

    private fun getRefreshToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshTime))
            .signWith(SignatureAlgorithm.HS512, refreshSecret)
            .compact()
    }

    private fun isSigned(jwt: String, secret: String): Boolean =
        Jwts.parser().setSigningKey(secret).isSigned(jwt)

    private fun parseJWT(jwt: String, secret: String) =
        Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt)

    fun getUserFromJWT(jwt: String?, secret: String = authSecret): User? =
        try {
            if (jwt == null || !isSigned(jwt, secret))
                null
            else
                parseJWT(jwt, secret).body
                    .issuer
                    .let { userRepository.findById(it.toInt()).orElse(null) }
        } catch (ex: SignatureException) {
            null
        }

    fun getUserDetailsFromContext(): UserContextDetails? =
        UserContextDetails.fromContext()

    fun getUserFromContext(): User? =
        getUserDetailsFromContext()?.user

    fun getJobSeekerFromContext(): JobSeeker? =
        getUserFromContext()
            ?.let { jobSeekerRepository.findByUserId(it.id!!).orElse(null) }

    fun getJobSeekerFromContextUnsafe(): JobSeeker =
        getJobSeekerFromContext() ?: throw UnauthenticatedException()

    fun getHrPartnerFromContext(): HrPartner? =
        getUserFromContext()
            ?.let { hrPartnerRepository.findByUserId(it.id!!).orElse(null) }

    fun getOrganizationFromContext(): Organization? =
        getUserFromContext()?.id
            ?.let { organizationRepository.findByUserId(it).orElse(null) }

    fun getTokens(user: User): Pair<String, String>? {
        val refreshJWT = getRefreshToken(user)
        val authJWT = getAuthenticateToken(user)
        if (refreshJWT == null || authJWT == null) return null
        return Pair(authJWT, refreshJWT)
    }

    fun refreshToken(token: String, userId: Int): String? {
        val refreshUser = getUserFromJWT(token, refreshSecret)
        val authUser = userRepository.findById(userId).orElse(null)
        if (refreshUser == authUser) return getAuthenticateToken(refreshUser!!)
        return null
    }

    fun checkUserRights(userId: Int): Boolean =
        getUserFromContext()?.let { it.id == userId } ?: false

    fun checkJobSeekerRights(id: Int) =
        checkUserRights(id) && getJobSeekerFromContext() != null

    fun checkHrRights(id: Int) =
        checkUserRights(id) && getHrPartnerFromContext() != null

    fun checkOrganizationRights(id: Int) =
        checkUserRights(id) && getOrganizationFromContext() != null

    fun checkOfferRights(offer: Offer): Boolean {
        getUserFromContext().let {
            return when (it!!.getUserType()) {
                "organization" ->
                    checkOrganizationRights(offer.creator.organization.user.id!!)
                "hr" ->
                    checkHrRights(offer.creator.user.id!!)
                else ->
                    false
            }
        }
    }

    fun checkOrganizationHrRights(hrId: Int) =
        getOrganizationFromContext()?.let {
            it.verified && hrPartnerRepository.findByUserId(hrId).get().organization == it
        } ?: false

    private fun User.getUserType(): String =
        jobSeekerRepository.findByUserId(this.id!!).orElse(null)?.let { "job_seeker" }
            ?: hrPartnerRepository.findByUserId(this.id).orElse(null)?.let { "hr" }
            ?: organizationRepository.findByUserId(this.id).orElse(null)?.let { "organization" }
            ?: throw InvalidUserException()

    fun isCorrectApiKey(apiKey: String?): Boolean = apiKey != null && apiKey == API_KEY
}

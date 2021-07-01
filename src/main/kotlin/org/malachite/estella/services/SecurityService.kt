package org.malachite.estella.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.people.domain.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Date
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Service
class SecurityService(
    @Autowired val userService: UserService,
    @Autowired val jobSeekerRepository: JobSeekerRepository,
    @Autowired val hrPartnerRepository: HrPartnerRepository
) {

    private val authSecret = "secret"
    private val refreshSecret = "refreshSecret"
    private val refreshTime = 3600 * 1000 * 24 // 1 day
    private val authTime = 15 * 60 * 1000 // 15 minutes

    private fun getAuthenticateToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + authTime))
            .signWith(SignatureAlgorithm.HS512, authSecret)
            .compact()
    }


    private fun getRefreshToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + refreshTime))
            .signWith(SignatureAlgorithm.HS512, refreshSecret)
            .compact()
    }

    private fun isSigned(jwt: String, secret: String) =
        Jwts.parser()
            .setSigningKey(secret)
            .isSigned(jwt)

    private fun parseJWT(jwt: String, secret: String) =
        Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(jwt)


    fun getUserFromJWT(jwt: String?, secret: String = authSecret): User? {
        if (jwt == null || !isSigned(jwt, secret)) return null
        val id = parseJWT(jwt, secret)
            .body
            .issuer
        return userService.getUser(id.toInt())
    }

    fun getJobSeekerFromJWT(jwt: String?): JobSeeker? {
        return getUserFromJWT(jwt)?.let { jobSeekerRepository.findByUserId(it.id!!).orElse(null) }
    }

    fun getHrPartnerFromJWT(jwt: String?): HrPartner? {
        return getUserFromJWT(jwt)?.let { hrPartnerRepository.findById(it.id!!).orElse(null) }
    }



    fun getTokens(user: User): Pair<String,String>? {
        val refreshJWT = getRefreshToken(user)
        val authJWT = getAuthenticateToken(user)
        if(refreshJWT==null || authJWT==null)return null
        return Pair(authJWT,refreshJWT)
    }


    fun refreshToken(token: String, userId: Int): String? {
        val refreshUser = getUserFromJWT(token, refreshSecret)
        val authUser = userService.getUser(userId)
        if (refreshUser == authUser)
            return getAuthenticateToken(refreshUser)
        return null
    }

    fun checkUserRights(jwt:String?,userId: Int):Boolean{
        val tokenUser = getUserFromJWT(jwt)
        return tokenUser != null && tokenUser.id ==userId
    }

}
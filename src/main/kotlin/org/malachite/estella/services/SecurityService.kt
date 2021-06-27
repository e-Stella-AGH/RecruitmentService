package org.malachite.estella.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.people.domain.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Date
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Service
class SecurityService(@Autowired val userRepository: UserRepository) {

    private val authSecret = "secret"
    private val refreshSecret = "refreshSecret"

    private fun getAuthenticateToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutes
            .signWith(SignatureAlgorithm.HS512, authSecret)
            .compact()
    }


    private fun getRefreshToken(user: User): String? {
        val issuer = user.id.toString()
        return Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + 3600 * 1000 * 24)) // 1 day
            .signWith(SignatureAlgorithm.HS512, refreshSecret)
            .compact()
    }

    private fun parseJWT(jwt: String, secret: String) =
        Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(jwt)


    fun getUserFromJWT(jwt: String?, secret: String = authSecret): User? {
        if (jwt == null) return null
        val id = parseJWT(jwt, secret)
            .body
            .issuer
        return userRepository.findById(id.toInt()).orElse(null)
    }

    fun setCookie(user: User, response: HttpServletResponse): Unit {
        val authJWT = getAuthenticateToken(user)
        val cookie = Cookie("jwt", authJWT)
        cookie.isHttpOnly = true
        response.addCookie(cookie)
    }

    fun getTokens(user: User, response: HttpServletResponse): String? {
        val refreshJWT = getRefreshToken(user)
        setCookie(user, response)
        return refreshJWT
    }

    fun deleteCookie(response: HttpServletResponse) {
        val cookie = Cookie("jwt", "")
        cookie.maxAge = 0
        response.addCookie(cookie)
    }

    fun refreshToken(token: String, response: HttpServletResponse): Unit? {
        val user = getUserFromJWT(token, refreshSecret)
        return user?.let { setCookie(user, response) } ?: null
    }


}
package org.malachite.estella.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.services.UserService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtSecurityFilter(val userService: UserService) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = request.getHeader(EStellaHeaders.jwtToken)
        authHeader?.let { token ->
            try {
                val userDetails = userService.getUserContextDetails(token)
                val auth = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails._authorities
                )
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth
            } catch (ex: SignatureException) {
                logger.error("Invalid JWT signature - " + ex.message)
            } catch (ex: MalformedJwtException) {
                logger.error("Invalid JWT token - " + ex.message)
            } catch (ex: ExpiredJwtException) {
                logger.error("Expired JWT token - " + ex.message)
            } catch (ex: UnsupportedJwtException) {
                logger.error("Unsupported JWT token - " + ex.message)
            } catch (ex: IllegalArgumentException) {
                logger.error("JWT claims string is empty - " + ex.message)
            }
        }
        filterChain.doFilter(request, response)
    }
}

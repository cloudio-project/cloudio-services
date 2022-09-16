package ch.hevs.cloudio.cloud.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AccessTokenFilter(
    private val accessTokenManager: AccessTokenManager,
    private val userDetailsService: CloudioUserDetailsService
): OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        request.getHeader("Authorization").let { header ->
            if (header != null && header.isNotEmpty() && header.startsWith("Bearer")) {
                header.split(" ").getOrNull(1)?.trim()?.let { token ->
                    accessTokenManager.validate(token)?.let { username ->
                        userDetailsService.loadUserByID(username).let { userDetails ->
                            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                                details = WebAuthenticationDetailsSource().buildDetails(request)
                            }
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
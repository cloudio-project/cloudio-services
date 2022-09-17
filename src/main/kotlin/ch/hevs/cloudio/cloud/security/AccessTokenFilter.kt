package ch.hevs.cloudio.cloud.security

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
                    accessTokenManager.validate(token).let { result ->
                        when(result) {
                            is AccessTokenManager.ValidUserToken -> userDetailsService.loadUserByID(result.userId).let { userDetails ->
                                SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                                    details = WebAuthenticationDetailsSource().buildDetails(request)
                                }
                            }

                            is AccessTokenManager.ValidEndpointPermissionToken ->
                                SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(result.id, result, listOf(Authority.HTTP_ACCESS).map { SimpleGrantedAuthority(it.name) })
                            
                            is AccessTokenManager.InvalidToken -> {}
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}

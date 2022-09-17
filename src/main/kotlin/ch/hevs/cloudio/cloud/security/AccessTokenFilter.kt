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
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        request.getHeader("Authorization").let { header ->
            if (header != null && header.isNotEmpty() && header.startsWith("Bearer")) {
                header.split(" ").getOrNull(1)?.trim()?.let { token ->
                    accessTokenManager.validate(token).let { result ->
                        try {
                            when (result) {
                                is AccessTokenManager.ValidUserToken -> SecurityContextHolder.getContext().authentication =
                                    UsernamePasswordAuthenticationToken(result.userDetails, null, result.userDetails.authorities).apply {
                                        details = WebAuthenticationDetailsSource().buildDetails(request)
                                    }

                                is AccessTokenManager.ValidEndpointPermissionToken -> SecurityContextHolder.getContext().authentication =
                                            AnonymousAuthenticationToken(result.hashCode().toString(), result, listOf(Authority.HTTP_ACCESS).map { SimpleGrantedAuthority(it.name) }).apply {
                                                details = WebAuthenticationDetailsSource().buildDetails(request)
                                            }


                                is AccessTokenManager.ValidEndpointGroupPermissionToken -> SecurityContextHolder.getContext().authentication =
                                            AnonymousAuthenticationToken(result.hashCode().toString(), result, listOf(Authority.HTTP_ACCESS).map { SimpleGrantedAuthority(it.name) }).apply {
                                                details = WebAuthenticationDetailsSource().buildDetails(request)
                                            }

                                else -> {}
                            }
                        } catch (_: Exception) { }
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}

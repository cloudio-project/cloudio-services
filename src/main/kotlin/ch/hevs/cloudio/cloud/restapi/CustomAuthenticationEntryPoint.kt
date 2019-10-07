package ch.hevs.cloudio.cloud.restapi

import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {

        response!!.writer.println("HTTP Status 401 -Access Denied")
        response!!.writer.println("message: " + authException?.message)

        when(authException){
            is CloudioAuthorityException ->
                response!!.writer.println("type: CloudioAuthorityException")

            is UsernameNotFoundException ->
                response!!.writer.println("type: UsernameNotFoundException")

            else ->
                response!!.writer.println("type: AuthenticationException")

        }

    }


}
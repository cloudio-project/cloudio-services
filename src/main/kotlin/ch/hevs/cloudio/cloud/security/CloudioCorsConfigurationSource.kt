package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.cors.CorsRepository
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import javax.servlet.http.HttpServletRequest

class CloudioCorsConfigurationSource(private val corsRepository: CorsRepository) : CorsConfigurationSource {

    override fun getCorsConfiguration(request: HttpServletRequest): CorsConfiguration? {
        //Create a basic cors config
        val config = CorsConfiguration().applyPermitDefaultValues()

        //add the http allowed methods
        config.addAllowedMethod(HttpMethod.GET)
        config.addAllowedMethod(HttpMethod.PUT)
        config.addAllowedMethod(HttpMethod.POST)
        config.addAllowedMethod(HttpMethod.DELETE)

        //empty the default allowed origin list
        config.allowedOrigins = listOf<String>()

        //add the cloudio allowed origins
        corsRepository.findAll().forEach {
            config.addAllowedOrigin(it.origin)
        }

        return config
    }
}
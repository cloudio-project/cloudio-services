package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.cors.CorsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import javax.servlet.http.HttpServletRequest

@Service
class CloudioCorsConfigurationSource : CorsConfigurationSource {

    @Autowired
    private lateinit var corsRepository: CorsRepository

    private var a = 0

    override fun getCorsConfiguration(request: HttpServletRequest): CorsConfiguration? {
        //Create a basic cors config
        val config = CorsConfiguration().applyPermitDefaultValues()

        config.addAllowedMethod(HttpMethod.GET)
        config.addAllowedMethod(HttpMethod.PUT)
        config.addAllowedMethod(HttpMethod.POST)
        config.addAllowedMethod(HttpMethod.DELETE)

        corsRepository.findAll().forEach {
            config.addAllowedOrigin(it.origin)
        }

        return config
    }
}
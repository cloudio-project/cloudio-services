package ch.hevs.cloudio.cloud.cors
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsProcessor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.cors.DefaultCorsProcessor

class MyCorsProcessor(private val corsRepository: CorsRepository) : CorsProcessor{
    private val defaultCorsProcessor: DefaultCorsProcessor = DefaultCorsProcessor()

    //TODO setProcessor

    override fun processRequest(config: CorsConfiguration?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        // override the spring cors configuration allowed origins
        corsRepository.findAll().forEach {
            if (config != null) {
                config.addAllowedOrigin(it.origin)
            }
        }

        //call the default cors processor
        return defaultCorsProcessor.processRequest(config, request, response)
    }
}
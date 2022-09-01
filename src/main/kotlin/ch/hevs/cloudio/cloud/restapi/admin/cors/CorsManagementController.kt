package ch.hevs.cloudio.cloud.restapi.admin.cors

import ch.hevs.cloudio.cloud.cors.CorsOrigin
import ch.hevs.cloudio.cloud.cors.CorsRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Profile("rest-api")
@Tag(name = "Cors Management", description = "Allows an admin user to manage cors allowed origins.")
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class CorsManagementController (
        private val corsRepository: CorsRepository
){
    @Operation(summary = "List all allowed origins.")
    @GetMapping("/cors")
    @ResponseStatus(HttpStatus.OK)
    fun getAllOrigins() = corsRepository.findAll().map {
        it.origin
    }

    @Operation(summary = "Add an origin.")
    @PostMapping("/cors")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun addOrigin(@RequestParam origin: String){
        if (corsRepository.existsByOrigin(origin)) {
            throw CloudioHttpExceptions.Conflict("Origin '${origin}' already allowed.")
        }
        corsRepository.save(
                CorsOrigin(origin)
        )
    }

    @Operation(summary = "Delete an origin.")
    @DeleteMapping("/cors")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteOrigin(@RequestParam origin: String){
        if (!corsRepository.existsByOrigin(origin)) {
            throw CloudioHttpExceptions.NotFound("Origin '${origin}' not found.")
        }
        corsRepository.deleteByOrigin(origin)
    }

}
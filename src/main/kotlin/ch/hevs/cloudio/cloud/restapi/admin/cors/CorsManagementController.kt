package ch.hevs.cloudio.cloud.restapi.admin.cors

import ch.hevs.cloudio.cloud.cors.CorsOrigin
import ch.hevs.cloudio.cloud.cors.CorsRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.services.WebSocketConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Profile("rest-api")
@Tag(name = "Cors Management", description = "Allows an admin user to manage cors allowed origins.")
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "basicAuth")
@Authority.HttpAdmin
class CorsManagementController (
        private val corsRepository: CorsRepository,
        private val webSocketConfig: WebSocketConfig
){
    @GetMapping("/cors", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List all allowed origins.")
    @ApiResponses(value = [
        ApiResponse(description = "List of all allowed origins.", responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(type = "string", example = "https://cloudio.hevs.ch")))]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun getAllOrigins() = corsRepository.findAll().map {
        it.origin
    }

    @PostMapping("/cors")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Add an allowed origin.")
    @ApiResponses(value = [
        ApiResponse(description = "The allowed origin was added.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "Origin is already allowed", responseCode = "409", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun postOrigin(@RequestParam origin: String) {
        if (corsRepository.existsByOrigin(origin)) {
            throw CloudioHttpExceptions.Conflict("Origin '${origin}' already allowed.")
        }
        corsRepository.save(
            CorsOrigin(origin)
        )
        webSocketConfig.updateAllowedOrigins()
    }

    @DeleteMapping("/cors")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Delete an allowed origin.")
    @ApiResponses(value = [
        ApiResponse(description = "The allowed origin was removed.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "Origin not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun deleteOrigin(@RequestParam origin: String){
        if (!corsRepository.existsByOrigin(origin)) {
            throw CloudioHttpExceptions.NotFound("Origin '${origin}' not found.")
        }
        corsRepository.deleteByOrigin(origin)
        webSocketConfig.updateAllowedOrigins()
    }
}

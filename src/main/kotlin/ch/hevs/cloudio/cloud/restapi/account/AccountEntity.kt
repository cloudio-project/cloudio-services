package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Account", description = "User account information.")
data class AccountEntity(
        @Schema(description = "Username.", readOnly = true, example = "d.vader")
        var name: String,

        @Schema(description ="Email address.", example = "d.vader@empire.gx")
        var email: String,

        @Schema(description ="System-wide user authorities.", readOnly = true, example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        var authorities: Set<Authority>,

        @Schema(description ="List of all groups the user is member of.", readOnly = true, example = "[\"EvilEmpire\"]")
        var groupMemberships: List<String>,

        @Schema(description ="User account metadata.", example = "{\"lightSaberColor\": \"red\", \"age\": 38}")
        var metadata: Map<String, Any>
)

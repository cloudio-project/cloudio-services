package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Account", description = "User account information.")
data class AccountEntity(
        @Schema(description = "Username.", readOnly = true, example = "john.doe")
        var name: String,

        @Schema(description ="Email address.", readOnly = true, example = "john.doe@theinternet.org")
        var email: String,

        @Schema(description ="System-wide user authorities.", readOnly = true, example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        var authorities: Set<Authority>,

        @Schema(description ="List of all groups the user is member of.", readOnly = true, example = "[\"Managers\"]")
        var groupMemberships: List<String>,

        @Schema(description ="User account metadata.", readOnly = true, example = "{\"location\": \"Sion\", \"age\": 38}")
        var metadata: Map<String, Any>
)

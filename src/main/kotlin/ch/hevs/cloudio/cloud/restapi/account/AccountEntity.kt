package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "User account information.")
data class AccountEntity(
        @ApiModelProperty(
                "Username.", position = 0, readOnly = true, example = "d.vader")
        var name: String,

        @ApiModelProperty("Email address.", position = 1, example = "d.vader@empire.gx")
        var email: String,

        @ApiModelProperty("System-wide user authorities.", position = 2, readOnly = true, example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        var authorities: Set<Authority>,

        @ApiModelProperty("List of all groups the user is member of.", position = 3, readOnly = true, example = "[\"EvilEmpire\"]")
        var groupMemberships: List<String>,

        @ApiModelProperty("User account metadata.", position = 4, example = "{\"lightSaberColor\": \"red\", \"age\": 38}")
        var metadata: Map<String, Any>
)

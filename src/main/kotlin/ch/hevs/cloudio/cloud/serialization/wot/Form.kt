package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Form(
        val href: String,
        val op: String,
        val subprotocol: String?
)
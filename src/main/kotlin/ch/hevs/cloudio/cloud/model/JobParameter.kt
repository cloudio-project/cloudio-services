package ch.hevs.cloudio.cloud.model

data class JobParameter(
        val jobURI: String,
        val correlationID: String,
        val data: String = "",
        val sendOutput: Boolean
)

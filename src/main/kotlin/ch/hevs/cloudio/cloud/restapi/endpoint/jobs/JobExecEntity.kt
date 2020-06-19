package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

// TODO: Document.
data class JobExecEntity(
        val jobURI: String,
        val enableOutput: Boolean,
        val correlationID: String,
        val arguments: String = "",
        val timeout: Long
)

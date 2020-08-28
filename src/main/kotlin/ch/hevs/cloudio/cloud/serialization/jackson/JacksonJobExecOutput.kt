package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.JobExecOutput
import java.util.*

data class JacksonJobExecOutput(
        var correlationID: Optional<String> = Optional.empty(),
        var data: Optional<String> = Optional.empty()
) {
    fun toJobExecOutput() = JobExecOutput(
            correlationID = correlationID.get(),
            output = data.get()
    )
}

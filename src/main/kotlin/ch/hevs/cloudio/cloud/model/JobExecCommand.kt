package ch.hevs.cloudio.cloud.model

/**
 * Send from the cloud to an endpoint in order to execute a job.
 * @see ActionIdentifier.JOB_EXECUTE
 */
data class JobExecCommand(
        /**
         * Correlation ID to send with the request to the endpoint has received in the @set message
         */
        val correlationID: String,

        /**
         * URI of the job to execute.
         */
        val jobURI: String,

        /**
         * Data to pass to the job.
         */
        val data: String = "",

        /**
         * If true, the endpoint sends the job's output back to the cloud, the endpoint sends no output at all.
         */
        // TODO: Add option to have: No output, line per line, complete output in a single message.
        val sendOutput: Boolean
)

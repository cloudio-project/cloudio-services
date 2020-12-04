package ch.hevs.cloudio.cloud.model

/**
 * Send from the endpoint to the cloud for each output line during the execution of a job.
 * @see ActionIdentifier.JOB_EXECUTE_OUTPUT
 */
data class JobExecOutput(
        /**
         * Copy of the correlation ID the endpoint has received in the @exec message.
         */
        var correlationID: String = "0",

        /**
         * Job's output.
         */
        var output: String = ""
)

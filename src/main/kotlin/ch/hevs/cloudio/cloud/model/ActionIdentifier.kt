package ch.hevs.cloudio.cloud.model

/**
 * Represents all actions (verbs) that are possible in cloud.iO and offers methods to parse and validate actions from string lists.
 *
 * An cloud.iO topic always starts with the action (or verb if you like). Those always have to start with an '@' character.
 */
enum class ActionIdentifier(
        private val verb: String,
        private val minModelLevels: Int,
        private val maxModelLevels: Int = Int.MAX_VALUE,
        private val wildcardAllowed: Boolean = false
) {
    /**
     * No action present.
     *
     * This can an action of a perfectly valid model identifier in the case no action is specified.
     */
    NONE("", 0),

    /**
     * Invalid action.
     *
     * This can be caused by either an invalid action (verb) is present or the minimal/maximal model identifier level constraint is not met when calling the method [ActionIdentifier.fromURI].
     * Each action defines a minimal and a maximal level count used to validate a passed URI.
     */
    INVALID("INVALID", 0, 0),

    /**
     * A message to a topic starting with the action (verb) is send by an endpoint every time a connection to the message broker has been established.
     * The message payload is of the type [EndpointDataModel].
     */
    ENDPOINT_ONLINE("@online", 1 , 1),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint every time a connection to the message broker has been closed ot lost (MQTT last will).
     * The message should have no payload.
     */
    ENDPOINT_OFFLINE("@offline", 1, 1),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint that is already connected in the event a node was added.
     * The message payload is of type [Node].
     */
    NODE_ADDED("@nodeAdded", 2, 2),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint every time a node is removed.
     * The message should have no payload.
     */
    NODE_REMOVED("@nodeRemoved", 2, 2),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint every time a single attribute (non-transactional) has been updated in an endpoint's data model.
     * In the case of transactional changes, a cloud service sends the individual update messages in the name of the endpoint.
     * The message payload is of type [Attribute].
     */
    ATTRIBUTE_UPDATE("@update", 4, Int.MAX_VALUE, true),

    /**
     * A message to a topic starting with this action (verb) is send to an endpoint in order to set the value of an attribute in an endpoint's data model.
     * The message payload is of type [Attribute].
     */
    ATTRIBUTE_SET("@set", 4, Int.MAX_VALUE, true),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint every time an attribute has successfully been modified by an [ActionIdentifier.ATTRIBUTE_SET] action.
     * The message payload is of type [AttributeSetStatus].
     */
    ATTRIBUTE_DID_SET("@didSet", 4, Int.MAX_VALUE, true),

    /**
     * A message to a topic starting with this action (verb) is send by an endpoint when a transaction is committed.
     * The message payload is of type [Transaction].
     */
    TRANSACTION("@transaction", 1, 1),

    /**
     * A message to a topic starting with this action (verb) is send to an endpoint in order to execute a job (script).
     * The message payload is of type [JobExecCommand].
     */
    JOB_EXECUTE("@exec", 1 , 1),

    /**
     * A message to a topic starting with this action (verb) is send from an endpoint every time a new output line of the currently executing job is available.
     * The message payload is of type [JobExecOutput].
     */
    JOB_EXECUTE_OUTPUT("@execOutput", 2, 2),

    /**
     * A message to a topic starting with this action (verb) is send from an endpoint in the case that during the time the endpoint had no connection to the message broker updates to attributes or
     * any other messages were queued for delivery to the cloud.
     * The message payload is of type [DelayedMessages].
     */
    DELAYED_MESSAGES("@delayed", 1, 1),

    /**
     * A message to a topic starting with this action (verb) is send from the cloud to an endpoint in order to change the endpoint's log level.
     * The message payload is of type [LogLevel].
     */
    LOG_LEVEL("@logsLevel", 1, 1),

    /**
     * A message to a topic starting with this action (verb) is send from an endpoint to the cloud for each log messages that has a level higher than the configured log level.
     * The message payload is of type [LogMessage].
     */
    LOG_OUTPUT("@logs", 1, 1);

    companion object {
        /**
         * Analyzes the passed list of strings (uri), returns the action and removes the action verb from the list.
         *
         * @param uri   List of strings that composes the model element's URI (model identifier).
         * @return      Action detected in the URI.
         */
        fun fromURI(uri: MutableList<String>) = when (val activity = uri.firstOrNull()) {
            ENDPOINT_ONLINE.verb -> {
                uri.removeAt(0)
                ENDPOINT_ONLINE
            }
            ENDPOINT_OFFLINE.verb -> {
                uri.removeAt(0)
                ENDPOINT_OFFLINE
            }
            NODE_ADDED.verb -> {
                uri.removeAt(0)
                NODE_ADDED
            }
            NODE_REMOVED.verb -> {
                uri.removeAt(0)
                NODE_REMOVED
            }
            ATTRIBUTE_UPDATE.verb -> {
                uri.removeAt(0)
                ATTRIBUTE_UPDATE
            }
            ATTRIBUTE_SET.verb -> {
                uri.removeAt(0)
                ATTRIBUTE_SET
            }
            ATTRIBUTE_DID_SET.verb -> {
                uri.removeAt(0)
                ATTRIBUTE_DID_SET
            }
            TRANSACTION.verb -> {
                uri.removeAt(0)
                TRANSACTION
            }
            JOB_EXECUTE.verb -> {
                uri.removeAt(0)
                JOB_EXECUTE
            }
            JOB_EXECUTE_OUTPUT.verb -> {
                uri.removeAt(0)
                JOB_EXECUTE_OUTPUT
            }
            DELAYED_MESSAGES.verb -> {
                uri.removeAt(0)
                DELAYED_MESSAGES
            }
            LOG_LEVEL.verb -> {
                uri.removeAt(0)
                LOG_LEVEL
            }
            LOG_OUTPUT.verb -> {
                uri.removeAt(0)
                LOG_OUTPUT
            }
            null -> {
                INVALID
            }
            else -> if (activity.startsWith("@")) {
                uri.removeAt(0)
                INVALID
            } else {
                NONE
            }
        }.let {
            when {
                uri.count() in it.minModelLevels..it.maxModelLevels -> it
                it.wildcardAllowed && uri.isNotEmpty() && uri.last() == "#" -> it
                else -> INVALID
            }
        }
    }

    /**
     * Converts the value into a string usable for topic creation.
     *
     * @return String representation suitable for topic construction.
     */
    override fun toString() = verb
}

package ch.hevs.cloudio.cloud.model

enum class ActionIdentifier(
        private val verb: String,
        private val minModelLevels: Int,
        private val maxModelLevels: Int = Int.MAX_VALUE
) {
    NONE("", 0),
    INVALID("INVALID", 0, 0),

    ENDPOINT_ONLINE("@online", 1 , 1),
    ENDPOINT_OFFLINE("@offline", 1, 1),

    NODE_ADDED("@nodeAdded", 2, 2),
    NODE_REMOVED("@nodeRemoved", 2, 2),

    ATTRIBUTE_UPDATE("@update", 4),
    ATTRIBUTE_SET("@set", 4),
    ATTRIBUTE_DID_SET("@didSet", 4),

    TRANSACTION("@transaction", 1, 1),

    JOB_EXECUTE("@exec", 1 , 1),
    JOB_EXECUTE_OUTPUT("@execOutput", 2, 2),

    DELAYED("@delayed", 1, 1);

    companion object {
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
            DELAYED.verb -> {
                uri.removeAt(0)
                DELAYED
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
            if (uri.count() in it.minModelLevels..it.maxModelLevels) it else INVALID
        }
    }

    override fun toString() = verb
}

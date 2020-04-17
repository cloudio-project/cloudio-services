package ch.hevs.cloudio.cloud.model

enum class ActionIdentifier(val value: String) {
    NONE(""),
    INVALID("INVALID"),

    ENDPOINT_ONLINE("@online"),
    ENDPOINT_OFFLINE("@offline"),

    NODE_ADDED("@nodeAdded"),
    NODE_REMOVED("@nodeRemoved"),

    ATTRIBUTE_UPDATE("@update"),
    ATTRIBUTE_SET("@set"),

    TRANSACTION("@transaction"),

    EXECUTE("@exec"),
    EXECUTE_OUTPUT("@execOutput"),

    DELAYED("@delayed");

    companion object {
        fun fromURI(uri: MutableList<String>) = when (val activity = uri.firstOrNull()) {
            ENDPOINT_ONLINE.value -> {
                uri.removeAt(0)
                ENDPOINT_ONLINE
            }
            ENDPOINT_OFFLINE.value -> {
                uri.removeAt(0)
                ENDPOINT_OFFLINE
            }
            NODE_ADDED.value -> {
                uri.removeAt(0)
                NODE_ADDED
            }
            NODE_REMOVED.value -> {
                uri.removeAt(0)
                NODE_REMOVED
            }
            ATTRIBUTE_UPDATE.value -> {
                uri.removeAt(0)
                ATTRIBUTE_UPDATE
            }
            ATTRIBUTE_SET.value -> {
                uri.removeAt(0)
                ATTRIBUTE_SET
            }
            TRANSACTION.value -> {
                uri.removeAt(0)
                TRANSACTION
            }
            EXECUTE.value -> {
                uri.removeAt(0)
                EXECUTE
            }
            EXECUTE_OUTPUT.value -> {
                uri.removeAt(0)
                EXECUTE_OUTPUT
            }
            DELAYED.value -> {
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
        }
    }
}

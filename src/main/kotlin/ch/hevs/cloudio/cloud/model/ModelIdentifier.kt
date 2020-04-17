package ch.hevs.cloudio.cloud.model

import java.util.*

class ModelIdentifier(uri: String) {
    val valid: Boolean
    val action: ActionIdentifier
    val endpoint: UUID
    val path: List<String>

    fun hasAction() = action != ActionIdentifier.NONE && action != ActionIdentifier.INVALID

    init {
        val splitURI = uri.split(".", "/").toMutableList()

        action = ActionIdentifier.fromURI(splitURI)
        if (action != ActionIdentifier.INVALID) {
            val uuid = try {
                UUID.fromString(splitURI.firstOrNull())
            } catch (e: Exception) {
                null
            }
            if (uuid != null) {
                valid = true
                endpoint = uuid
                splitURI.removeAt(0)
                path = splitURI
            } else {
                valid = false
                endpoint = UUID(0, 0)
                path = emptyList()
            }
        } else {
            valid = false
            endpoint = UUID(0, 0)
            path = emptyList()
        }
    }

    override fun toString() = if (valid)
        (if (action == ActionIdentifier.NONE) mutableListOf() else mutableListOf(action.value)).apply {
            add("$endpoint")
            addAll(path)
        }.joinToString(".")
    else ""
}

package ch.hevs.cloudio.cloud.model

import java.util.*

class ModelIdentifier(uri: String) {
    val valid: Boolean
    val action: ActionIdentifier
    val endpoint: UUID
    private val segments: List<String>

    fun count() = segments.count()
    operator fun get(index: Int) = segments[index]

    init {
        val splitURI = uri.split('.', '/').toMutableList()

        action = ActionIdentifier.fromURI(splitURI)
        if (action != ActionIdentifier.INVALID) {
            val uuid = try {
                UUID.fromString(splitURI.firstOrNull())
            } catch (e: Exception) {
                null
            }
            if (uuid != null && (uuid.leastSignificantBits != 0L || uuid.mostSignificantBits != 0L)) {
                valid = true
                endpoint = uuid
                splitURI.removeAt(0)
                segments = splitURI
            } else {
                valid = false
                endpoint = UUID(0, 0)
                segments = emptyList()
            }
        } else {
            valid = false
            endpoint = UUID(0, 0)
            segments = emptyList()
        }
    }

    fun toString(separator: Char) = if (valid)
        (if (action == ActionIdentifier.NONE) mutableListOf() else mutableListOf(action.toString())).apply {
            add("$endpoint")
            addAll(segments)
        }.joinToString("$separator")
    else ""

    override fun toString() = toString(separator = '.')
}

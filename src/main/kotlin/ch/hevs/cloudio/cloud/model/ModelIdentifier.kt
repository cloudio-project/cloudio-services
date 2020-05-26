package ch.hevs.cloudio.cloud.model

import java.io.Serializable
import java.util.*

class ModelIdentifier(uri: String) : Serializable {
    val valid: Boolean
    val action: ActionIdentifier
    val endpoint: UUID
    private val modelPath: List<String>

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
                modelPath = splitURI
            } else {
                valid = false
                endpoint = UUID(0, 0)
                modelPath = emptyList()
            }
        } else {
            valid = false
            endpoint = UUID(0, 0)
            modelPath = emptyList()
        }
    }

    fun count() = modelPath.count()
    operator fun get(index: Int) = modelPath[index]

    fun modelPath(separator: Char = '/') = modelPath.joinToString("$separator")

    fun toString(separator: Char) = if (valid)
        (if (action == ActionIdentifier.NONE) mutableListOf() else mutableListOf(action.toString())).apply {
            add("$endpoint")
            addAll(modelPath)
        }.joinToString("$separator")
    else ""

    override fun toString() = toString(separator = '/')

    override fun equals(other: Any?) = other is ModelIdentifier &&
            other.action == action &&
            other.endpoint == endpoint &&
            other.modelPath == modelPath

    override fun hashCode(): Int {
        return Objects.hash(action, endpoint, *modelPath.toTypedArray())
    }
}

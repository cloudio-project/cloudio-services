package ch.hevs.cloudio.cloud.model

import java.io.Serializable
import java.util.*

class ModelIdentifier(uri: String) : Serializable {
    val valid: Boolean
    var action: ActionIdentifier
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
            if (uuid != null && (uuid.leastSignificantBits != 0L || uuid.mostSignificantBits != 0L) && splitURI.none { it.isEmpty() }) {
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

    fun toAmqpTopic() = "$action.$endpoint.${modelPath('.')}"

    fun toInfluxSeriesName() = "$endpoint.${ modelPath('.')}"

    fun resolve(endpoint: Endpoint): Optional<Any> = Optional.ofNullable(when(count()) {
        0 -> endpoint
        1 -> endpoint.nodes[this[0]]
        else -> endpoint.nodes[this[0]]?.let { resolveOnNode(it) }
    })

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

    private fun resolveOnNode(node: Node): Any? = when(count()) {
        2 -> node.objects[this[1]]
        else -> node.objects[this[1]]?.let { resolveOnObject(it, 2) }
    }

    private fun resolveOnObject(obj: CloudioObject, index: Int): Any? = when(index) {
        count() - 1 -> obj.attributes[this[index]] ?: obj.objects[this[index]]
        else -> obj.objects[this[index]]?.let { resolveOnObject(it, index + 1) }
    }
}

package ch.hevs.cloudio.cloud.model

import java.io.Serializable
import java.util.*

class ModelIdentifier(uri: String) : Serializable {
    val valid: Boolean
    var action: ActionIdentifier
    val endpoint: UUID
    private val modelPath: List<String>

    init {
        // Split topic using '/' for REST format and '.' for AMQP format.
        var splitURI = uri.split('.', '/').toMutableList()

        // Convert from v0.1 endpoint topic format to new format if required.
        val nodesIndex = splitURI.indexOf("nodes")
        if (nodesIndex in 1..2) {
            splitURI = splitURI.filterIndexed { i, _ ->
                i < nodesIndex || (i - nodesIndex).rem(2) != 0
            }.toMutableList()
        }

        // Detect action and ensure action is valid.
        action = ActionIdentifier.fromURI(splitURI)
        if (action != ActionIdentifier.INVALID) {

            // Extract UUID and ensure that it is a valid UUID and no component of the path is empty.
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
    fun last() = modelPath.last()

    fun modelPath(separator: Char = '/') = modelPath.joinToString("$separator")

    fun toAMQPTopic() = if (valid && action != ActionIdentifier.NONE) "$action.$endpoint${if (count() > 0) "." else ""}${modelPath('.')}" else ""

    fun toAMQPTopicForVersion01Endpoints() = if (valid && action != ActionIdentifier.NONE)
        "$action.$endpoint${if (count() > 0) "." else ""}" + modelPath.mapIndexed { i, s ->
            when (i) {
                0 -> "nodes.$s"
                modelPath.size - 1 -> "attributes.$s"
                else -> "objects.$s"
            }
        }.joinToString(".")
    else ""

    fun toInfluxSeriesName() = if (valid && count() > 0) "$endpoint.${modelPath('.')}" else ""

    fun resolve(endpoint: EndpointDataModel): Optional<Any> = Optional.ofNullable(when (count()) {
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

    private fun resolveOnNode(node: Node): Any? = when (count()) {
        2 -> node.objects[this[1]]
        else -> node.objects[this[1]]?.let { resolveOnObject(it, 2) }
    }

    private fun resolveOnObject(obj: CloudioObject, index: Int): Any? = when (index) {
        count() - 1 -> obj.attributes[this[index]] ?: obj.objects[this[index]]
        else -> obj.objects[this[index]]?.let { resolveOnObject(it, index + 1) }
    }
}

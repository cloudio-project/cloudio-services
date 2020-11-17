package ch.hevs.cloudio.cloud.model

import java.io.Serializable
import java.util.*

/**
 * Identifies an element in the cloud.iO data model. This can be an endpoint, a node, an object, an attribute or a special function/entity (like jobs, logs, delayed message,...).
 *
 * @param uri   URI of the model identifier.
 */
class ModelIdentifier(uri: String) : Serializable {
    /**
     * If true the model identifier is valid.
     * Note that as many constraints as possible are already checked for validity during the construction of an object.
     */
    val valid: Boolean

    /**
     * The action if present.
     * If no action is present (can be the case when referring a REST resource) the value will be [ActionIdentifier.NONE].
     * If the value is [ActionIdentifier.INVALID], the identifier seems not to be valid.
     */
    var action: ActionIdentifier

    /**
     * UUID of the endpoint. Always present if the model identifier is valid.
     */
    val endpoint: UUID

    val wildcard: Boolean

    private val modelPath: List<String>

    init {
        // Split topic using '/' for REST format and '.' for AMQP format.
        var splitURI = uri.split('.', '/').toMutableList()

        // Is it a wildcard model index?
        wildcard = splitURI.last() == "#"

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
            if (uuid != null && (uuid.leastSignificantBits != 0L || uuid.mostSignificantBits != 0L) &&
                    splitURI.none { it.isEmpty() } && splitURI.subList(0, splitURI.count() - 1).none { it == "#" }) {
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

    /**
     * Returns the number of path components (excluding action and endpoint UUID) are present.
     *
     * @return Number of path components.
     */
    fun count() = modelPath.count()

    /**
     * Returns the path component at the specified position.
     *
     * @param index Position.
     * @return      Path component at requested index.
     */
    operator fun get(index: Int) = modelPath[index]

    /**
     * Returns the last path component.
     *
     * @return  Last path component.
     */
    fun last() = modelPath.last()

    /**
     * Returns the model path with the requested separator.
     *
     * @param separator Seperator to use for path construction. Defaults to '/'.
     * @return          Model path.
     */
    fun toModelPath(separator: Char = '/') = modelPath.joinToString("$separator")

    /**
     * Returns the corresponding AMQP topic.
     *
     * @return AMQP topic.
     */
    fun toAMQPTopic() = if (valid && action != ActionIdentifier.NONE) "$action.$endpoint${if (count() > 0) "." else ""}${toModelPath('.')}" else ""

    /**
     * Returns the corresponding AMQP topic in the v0.1 format for compatibility with older endpoints.
     *
     * @return AMQP topic on v0.1 format.
     */
    fun toAMQPTopicForMessageFormat1Endpoints() = if (valid && action != ActionIdentifier.NONE)
        "$action.$endpoint${if (count() > 0) "." else ""}" + modelPath.mapIndexed { i, s ->
            when (i) {
                0 -> "nodes.$s"
                modelPath.size - 1 -> "attributes.$s"
                else -> "objects.$s"
            }
        }.joinToString(".")
    else ""

    /**
     * Returns the corresponding ID used to identify the time series in InfluxDB.
     *
     * @return InfluxDB series name.
     */
    fun toInfluxSeriesName() = if (valid && count() > 0) "$endpoint.${toModelPath('.')}" else ""

    /**
     * Searches the endpoint data model element referenced by itself in the passed endpoint data model.
     *
     * Note that the method does not check that the endpoint's UUID matches!
     *
     * @param endpoint  Endpoint to search for the data model element.
     * @return          Either the endpoint data model element referenced by itself or nothing if not found.
     */
    fun resolve(endpoint: EndpointDataModel): Optional<Any> = Optional.ofNullable(if (!valid) null else when (count()) {
        0 -> endpoint
        1 -> endpoint.nodes[this[0]]
        else -> endpoint.nodes[this[0]]?.let { resolveOnNode(it) }
    })

    /**
     * Returns a string representation of the model identifier.
     *
     * @return String representation.
     */
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

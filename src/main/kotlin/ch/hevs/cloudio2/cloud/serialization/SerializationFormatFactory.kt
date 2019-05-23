package ch.hevs.cloudio2.cloud.serialization

object SerializationFormatFactory {
    private val formats: MutableList<SerializationFormat> = mutableListOf(
            JsonSerializationFormat
    )

    fun serializationFormat(data: ByteArray): SerializationFormat? = formats.find { it.detect(data) }

    fun registerSerializationFormat(serializationFormat: SerializationFormat) = formats.add(serializationFormat)
}
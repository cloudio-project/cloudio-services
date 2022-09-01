package ch.hevs.cloudio.cloud.serialization

import org.junit.Test

class FormatDetectionTests {
    @Test
    fun detect() {
        val formats = listOf(JSONSerializationFormat(), CBORSerializationFormat())

        (0..255).forEach {
            val format = formats.detect(byteArrayOf(it.toByte()))
            when {
                it == '{'.code -> assert(format?.identifier() == "JSON")
                it and 0b11100000 == 0b10100000 -> assert(format?.identifier() == "CBOR")
                else -> assert(format == null)
            }
        }
    }
}

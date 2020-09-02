package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.*
import co.nstant.`in`.cbor.model.Map
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

/*
 * JSON converted to CBOR using http://cbor.me.
 * Then replaced "//" by "//" and "([0-9,A-F]{2})" by "0x$1, ".
 * Removed last coma.
 */

class CBORSerializationFormatTests {

    /*
     * Endpoint data model deserialization.
     */

    @Test
    fun endpointDataModelNoNodesDeserialize() {
        val endpointDataModel = CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                0xA3,                                      // map(3)
                0x67,                                   // text(7)
                0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E,                    // "version"
                0x64,                                   // text(4)
                0x76, 0x30, 0x2E, 0x32,                          // "v0.2"
                0x70,                                   // text(0x16, )
                0x73, 0x75, 0x70, 0x70, 0x6F, 0x72, 0x74, 0x65, 0x64, 0x46, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x73,  // "supportedFormats"
                0x82,                                   // array(2)
                0x64,                                // text(4)
                0x4A, 0x53, 0x4F, 0x4E,                       // "JSON"
                0x64,                                // text(4)
                0x43, 0x42, 0x4F, 0x52,                       // "0xCB, OR"
                0x65,                                   // text(5)
                0x6E, 0x6F, 0x64, 0x65, 0x73,                        // "nodes"
                0xA0                                // map(0)
        ).map(Int::toByte).toByteArray())
        assert(endpointDataModel.version == "v0.2")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelThreeNodesDeserialize() {
        val endpointDataModel = CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                0xA3,                                     // map(3)
                0x67,                                  // text(7)
                0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E,                   // "version"
                0x64,                                  // text(4)
                0x76, 0x30, 0x2E, 0x32,                         // "v0.2"
                0x70,                                  // text(0x16,)
                0x73, 0x75, 0x70, 0x70, 0x6F, 0x72, 0x74, 0x65, 0x64, 0x46, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x73, // "supportedFormats"
                0x82,                                  // array(2)
                0x64,                               // text(4)
                0x4A, 0x53, 0x4F, 0x4E,                      // "JSON"
                0x64,                               // text(4)
                0x43, 0x42, 0x4F, 0x52,                      // "0xCB,OR"
                0x65,                                  // text(5)
                0x6E, 0x6F, 0x64, 0x65, 0x73,                       // "nodes"
                0xA3,                                  // map(3)
                0x63,                               // text(3)
                0x6F, 0x6E, 0x65,                        // "one"
                0xA2,                               // map(2)
                0x6A,                            // text(0x10,)
                0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,       // "implements"
                0x80,                            // array(0)
                0x67,                            // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,             // "objects"
                0xA0,                            // map(0)
                0x63,                               // text(3)
                0x74, 0x77, 0x6F,                        // "two"
                0xA2,                               // map(2)
                0x6A,                            // text(0x10,)
                0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,       // "implements"
                0x80,                            // array(0)
                0x67,                            // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,             // "objects"
                0xA0,                            // map(0)
                0x65,                               // text(5)
                0x74, 0x68, 0x72, 0x65, 0x65,                    // "three"
                0xA2,                               // map(2)
                0x6A,                            // text(0x10,)
                0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,       // "implements"
                0x80,                            // array(0)
                0x67,                            // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,             // "objects"
                0xA0                            // map(0)
        ).map(Int::toByte).toByteArray())
        assert(endpointDataModel.version == "v0.2")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.count() == 3)
    }

    @Test
    fun endpointDataModelNoVersionDeserialize() {
        val endpointDataModel = CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                0xA2,                                     // map(2)
                0x70,                                  // text(0x16,)
                0x73, 0x75, 0x70, 0x70, 0x6F, 0x72, 0x74, 0x65, 0x64, 0x46, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x73, // "supportedFormats"
                0x82,                                  // array(2)
                0x64,                               // text(4)
                0x4A, 0x53, 0x4F, 0x4E,                      // "JSON"
                0x64,                               // text(4)
                0x43, 0x42, 0x4F, 0x52,                      // "0xCB,OR"
                0x65,                                  // text(5)
                0x6E, 0x6F, 0x64, 0x65, 0x73,                       // "nodes"
                0xA0                                  // map(0)
        ).map(Int::toByte).toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelNoVersionNoSupportedFormatsDeserialize() {
        val endpointDataModel = CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                0xA1,               // map(1)
                0x65,            // text(5)
                0x6E, 0x6F, 0x64, 0x65, 0x73, // "nodes"
                0xA0            // map(0)
        ).map(Int::toByte).toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelNoNodesPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                    0xA2,                                     // map(2)
                    0x67,                                  // text(7)
                    0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E,                   // "version"
                    0x64,                                  // text(4)
                    0x76, 0x30, 0x2E, 0x32,                         // "v0.2"
                    0x70,                                  // text(0x16,)
                    0x73, 0x75, 0x70, 0x70, 0x6F, 0x72, 0x74, 0x65, 0x64, 0x46, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x73, // "supportedFormats"
                    0x82,                                  // array(2)
                    0x64,                               // text(4)
                    0x4A, 0x53, 0x4F, 0x4E,                      // "JSON"
                    0x64,                               // text(4)
                    0x43, 0x42, 0x4F, 0x52                      // "0xCB,OR"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelV1NoSupportedFormatsDeserialize() {
        val endpointDataModel = CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                0xA2,                   // map(2)
                0x67,                // text(7)
                0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E, // "version"
                0x64,                // text(4)
                0x76, 0x30, 0x2E, 0x31,       // "v0.1"
                0x65,                // text(5)
                0x6E, 0x6F, 0x64, 0x65, 0x73,     // "nodes"
                0xA0                // map(0)
        ).map(Int::toByte).toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelEmptyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(0x0A).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelV2NoSupportedFormatsDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                    0xA2,                   // map(2)
                    0x67,                // text(7)
                    0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E, // "version"
                    0x64,                // text(4)
                    0x76, 0x30, 0x2E, 0x32,       // "v0.2"
                    0x65,                // text(5)
                    0x6E, 0x6F, 0x64, 0x65, 0x73,     // "nodes"
                    0xA0                // map(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelAdditionalPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeEndpointDataModel(arrayOf(
                    0xA4,                                     // map(4)
                    0x67,                                  // text(7)
                    0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E,                   // "version"
                    0x64,                                  // text(4)
                    0x76, 0x30, 0x2E, 0x32,                         // "v0.2"
                    0x70,                                  // text(0x16,)
                    0x73, 0x75, 0x70, 0x70, 0x6F, 0x72, 0x74, 0x65, 0x64, 0x46, 0x6F, 0x72, 0x6D, 0x61, 0x74, 0x73, // "supportedFormats"
                    0x82,                                  // array(2)
                    0x64,                               // text(4)
                    0x4A, 0x53, 0x4F, 0x4E,                      // "JSON"
                    0x64,                               // text(4)
                    0x43, 0x42, 0x4F, 0x52,                      // "0xCB,OR"
                    0x65,                                  // text(5)
                    0x6E, 0x6F, 0x64, 0x65, 0x73,                       // "nodes"
                    0xA0,                                  // map(0)
                    0x66,                                  // text(6)
                    0x69, 0x73, 0x5F, 0x66, 0x75, 0x6E,                     // "is_fun"
                    0xF5                                  // primitive(0x21,)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    /*
     * Node deserialization.
     */

    @Test
    fun nodeNoObjectsDeserialize() {
        val node = CBORSerializationFormat().deserializeNode(arrayOf(
                0xA2,                            // map(2)
                0x6A,                         // text(0x10,)
                0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,    // "implements"
                0x82,                         // array(2)
                0x6A,                      // text(0x10,)
                0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x41, // "InterfaceA"
                0x6A,                      // text(0x10,)
                0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x42, // "InterfaceB"
                0x67,                         // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,          // "objects"
                0xA0                         // map(0)
        ).map(Int::toByte).toByteArray())
        assert(!node.online)
        assert(node.implements.count() == 2 && node.implements == setOf("InterfaceA", "InterfaceB"))
        assert(node.objects.count() == 0)
    }

    @Test
    fun nodeTwoObjectsDeserialize() {
        val node = CBORSerializationFormat().deserializeNode(arrayOf(
                0xA2,                               // map(2)
                0x6A,                            // text(0x10,)
                0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,       // "implements"
                0x82,                            // array(2)
                0x6A,                         // text(0x10,)
                0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x41,    // "InterfaceA"
                0x6A,                         // text(0x10,)
                0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x42,    // "InterfaceB"
                0x67,                            // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,             // "objects"
                0xA2,                            // map(2)
                0x64,                         // text(4)
                0x6F, 0x62, 0x6A, 0x31,                // "obj1"
                0xA3,                         // map(3)
                0x68,                      // text(8)
                0x63, 0x6F, 0x6E, 0x66, 0x6F, 0x72, 0x6D, 0x73,     // "conforms"
                0xF6,                      // primitive(0x22,)
                0x67,                      // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,       // "objects"
                0xA0,                      // map(0)
                0x6A,                      // text(0x10,)
                0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73, // "attributes"
                0xA0,                      // map(0)
                0x64,                         // text(4)
                0x6F, 0x62, 0x6A, 0x32,                // "obj2"
                0xA3,                         // map(3)
                0x68,                      // text(8)
                0x63, 0x6F, 0x6E, 0x66, 0x6F, 0x72, 0x6D, 0x73,     // "conforms"
                0xF6,                      // primitive(0x22,)
                0x67,                      // text(7)
                0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,       // "objects"
                0xA0,                      // map(0)
                0x6A,                      // text(0x10,)
                0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73, // "attributes"
                0xA0                      // map(0)
        ).map(Int::toByte).toByteArray())
        assert(!node.online)
        assert(node.implements.count() == 2 && node.implements == setOf("InterfaceA", "InterfaceB"))
        assert(node.objects.count() == 2)
    }

    @Test
    fun nodeNoImplementsDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeNode(arrayOf(
                    0xA1,                   // map(1)
                    0x67,                // text(7)
                    0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73, // "objects"
                    0xA0                // map(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    @Test
    fun nodeNoObjectsPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeNode(arrayOf(
                    0xA1,                            // map(1)
                    0x6A,                         // text(0x10,)
                    0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,    // "implements"
                    0x82,                         // array(2)
                    0x6A,                      // text(0x10,)
                    0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x41, // "InterfaceA"
                    0x6A,                      // text(0x10,)
                    0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x42 // "InterfaceB"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    @Test
    fun nodeUnknownPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeNode(arrayOf(
                    0xA3,                            // map(3)
                    0x65,                         // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,              // "level"
                    0x62,                         // text(2)
                    0x34, 0x34,                    // "0x44,"
                    0x6A,                         // text(0x10,)
                    0x69, 0x6D, 0x70, 0x6C, 0x65, 0x6D, 0x65, 0x6E, 0x74, 0x73,    // "implements"
                    0x82,                         // array(2)
                    0x6A,                      // text(0x10,)
                    0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x41, // "InterfaceA"
                    0x6A,                      // text(0x10,)
                    0x49, 0x6E, 0x74, 0x65, 0x72, 0x66, 0x61, 0x63, 0x65, 0x42, // "InterfaceB"
                    0x67,                         // text(7)
                    0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x73,          // "objects"
                    0xA2,                         // map(2)
                    0x64,                      // text(4)
                    0x6F, 0x62, 0x6A, 0x31,             // "obj1"
                    0xA0,                      // map(0)
                    0x64,                      // text(4)
                    0x6F, 0x62, 0x6A, 0x32,             // "obj2"
                    0xA0                      // map(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    /*
     * Attribute serialization.
     */

    @Test
    fun staticBooleanAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Boolean,
                timestamp = 1.2345,
                value = false
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Static")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Boolean")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.2345)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.SIMPLE_VALUE)
                assert((this as SimpleValue) == SimpleValue.FALSE)
            }
        }
    }

    @Test
    fun staticIntegerAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Integer,
                timestamp = 1.2346,
                value = 42
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Static")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Integer")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.2346)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNSIGNED_INTEGER)
                assert((this as UnsignedInteger).value.toInt() == 42)
            }
        }
    }

    @Test
    fun staticNumberAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Number,
                timestamp = 1.2347,
                value = 42.24
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Static")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Number")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.2347)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 42.24)
            }
        }
    }

    @Test
    fun staticStringAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.String,
                timestamp = 1.2348,
                value = "TEST123"
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Static")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "String")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.2348)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "TEST123")
            }
        }
    }

    @Test
    fun parameterBooleanAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Boolean,
                timestamp = 1.5533,
                value = true
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Parameter")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Boolean")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.5533)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.SIMPLE_VALUE)
                assert((this as SimpleValue) == SimpleValue.TRUE)
            }
        }
    }

    @Test
    fun parameterIntegerAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Integer,
                timestamp = 1.5544,
                value = 666
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Parameter")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Integer")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.5544)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNSIGNED_INTEGER)
                assert((this as UnsignedInteger).value.toInt() == 666)
            }
        }
    }

    @Test
    fun parameterNumberAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Number,
                timestamp = 1.5533,
                value = 123.456
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Parameter")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Number")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.5533)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 123.456)
            }
        }
    }

    @Test
    fun parameterStringAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.String,
                timestamp = 1.5522,
                value = "test_string"
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Parameter")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "String")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 1.5522)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "test_string")
            }
        }
    }

    @Test
    fun setPointBooleanAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Boolean,
                timestamp = 8888.5555,
                value = false
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "SetPoint")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Boolean")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 8888.5555)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.SIMPLE_VALUE)
                assert((this as SimpleValue) == SimpleValue.FALSE)
            }
        }
    }

    @Test
    fun setPointIntegerAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Integer,
                timestamp = 7777.4444,
                value = 1977
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "SetPoint")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Integer")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 7777.4444)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNSIGNED_INTEGER)
                assert((this as UnsignedInteger).value.toInt() == 1977)
            }
        }
    }

    @Test
    fun setPointNumberAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Number,
                timestamp = 6666.3333,
                value = 3.1415
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "SetPoint")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Number")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 6666.3333)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 3.1415)
            }
        }
    }

    @Test
    fun setPointStringAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.String,
                timestamp = 5555.2222,
                value = "aSetPoint"
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "SetPoint")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "String")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 5555.2222)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "aSetPoint")
            }
        }
    }

    @Test
    fun statusBooleanAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Boolean,
                timestamp = 12345678.22,
                value = true
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Status")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Boolean")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 12345678.22)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.SIMPLE_VALUE)
                assert((this as SimpleValue) == SimpleValue.TRUE)
            }
        }
    }

    @Test
    fun statusIntegerAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Integer,
                timestamp = 12345678.33,
                value = -69
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Status")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Integer")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 12345678.33)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.NEGATIVE_INTEGER)
                assert((this as NegativeInteger).value.toInt() == -69)
            }
        }
    }

    @Test
    fun statusNumberAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Number,
                timestamp = 12345678.44,
                value = 2.7182
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Status")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Number")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 12345678.44)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 2.7182)
            }
        }
    }

    @Test
    fun statusStringAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.String,
                timestamp = 12345678.55,
                value = "My Status"
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Status")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "String")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 12345678.55)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "My Status")
            }
        }
    }

    @Test
    fun measureBooleanAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Boolean,
                timestamp = 11223344.22,
                value = false
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Measure")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Boolean")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 11223344.22)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.SIMPLE_VALUE)
                assert((this as SimpleValue) == SimpleValue.FALSE)
            }
        }
    }

    @Test
    fun measureIntegerAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Integer,
                timestamp = 11223344.33,
                value = 80486
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Measure")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Integer")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 11223344.33)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNSIGNED_INTEGER)
                assert((this as UnsignedInteger).value.toInt() == 80486)
            }
        }
    }

    @Test
    fun measureNumberAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Number,
                timestamp = 11223344.44,
                value = -22.5
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Measure")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Number")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 11223344.44)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == -22.5)
            }
        }
    }

    @Test
    fun measureStringAttributeSerialize() {
        val cbor = CBORSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.String,
                timestamp = 11223344.55,
                value = "lorem ipsum"
        ))
        val decoded = CborDecoder(ByteArrayInputStream(cbor)).decode()
        assert(decoded.count() == 1)
        assert(decoded.first().majorType == MajorType.MAP)
        decoded.first().let { it as Map }.let { root ->
            root[UnicodeString("constraint")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "Measure")
            }
            root[UnicodeString("type")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "String")
            }
            root[UnicodeString("timestamp")].run {
                assert(majorType == MajorType.SPECIAL)
                assert((this as Special).specialType == SpecialType.IEEE_754_DOUBLE_PRECISION_FLOAT)
                assert((this as DoublePrecisionFloat).value == 11223344.55)
            }
            root[UnicodeString("value")].run {
                assert(majorType == MajorType.UNICODE_STRING)
                assert((this as UnicodeString).string == "lorem ipsum")
            }
        }
    }

    @Test
    fun invalidConstraintAttributeSerialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().serializeAttribute(Attribute(
                    constraint = AttributeConstraint.Invalid,
                    type = AttributeType.String,
                    timestamp = 1.0,
                    value = "should not work"
            ))
        }
        assert(exception.message == "Error serializing attribute.")
    }

    @Test
    fun invalidTypeAttributeSerialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().serializeAttribute(Attribute(
                    constraint = AttributeConstraint.Measure,
                    type = AttributeType.Invalid,
                    timestamp = 1.0,
                    value = "should not work"
            ))
        }
        assert(exception.message == "Error serializing attribute.")
    }

    @Test
    fun typeAndValueDoNotMatchAttributeSerialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().serializeAttribute(Attribute(
                    constraint = AttributeConstraint.Measure,
                    type = AttributeType.Integer,
                    timestamp = 1.0,
                    value = "should not work"
            ))
        }
        assert(exception.message == "Error serializing attribute.")
    }

    /*
     * Attribute deserialization.
     */

    @Test
    fun staticBooleanAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,   // map(4)
                0x6A,   // text(10)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                0x66, // text(6)
                0x53, 0x74, 0x61, 0x74, 0x69, 0x63, // "Static"
                0x64,   // text(4)
                0x74, 0x79, 0x70, 0x65, // "type"
                0x67,   // text(7)
                0x42, 0x6F, 0x6F, 0x6C, 0x65, 0x61, 0x6E,   // "Boolean"
                0x69,   // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,   // "timestamp"
                0xFB, 0x3F, 0xF3, 0xC0, 0x83, 0x12, 0x6E, 0x97, 0x8D,   // primitive(4608238512912635789)
                0x65,   // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,   // "value"
                0xF4    // primitive(20)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 1.2345)
        assert(attribute.value == false)
    }

    @Test
    fun staticIntegerAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,   // map(4)
                0x6A,   // text(10)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                0x66, // text(6)
                0x53, 0x74, 0x61, 0x74, 0x69, 0x63, // "Static"
                0x64,   // text(4)
                0x74, 0x79, 0x70, 0x65, // "type"
                0x67,   // text(7)
                0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,   // "Integer"
                0x69,   // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,   // "timestamp"
                0xFB, 0x3F, 0xF3, 0xC0, 0xEB, 0xED, 0xFA, 0x43, 0xFE,   // primitive(4608238963272598526)
                0x65,   // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,   // "value"
                0x18, 0x2A // unsigned(42)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 1.2346)
        assert(attribute.value == 42.toLong())
    }

    @Test
    fun staticNumberAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,   // map(4)
                0x6A,   // text(10)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, //"constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,          // "Number"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x3F, 0xF3, 0xC1, 0x54, 0xC9, 0x85, 0xF0, 0x6F,      // primitive(0x46, 0x08, 0x23, 0x94, 0x13, 0x63, 0x25, 0x61, 0x26, 3)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xFB, 0x40, 0x45, 0x1E, 0xB8, 0x51, 0xEB, 0x85, 0x1F      // primitive(0x46, 0x31, 0x14, 0x15, 0x68, 0x81, 0x76, 0x28, 0x44, 7)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 1.2347)
        assert(attribute.value == 42.24)
    }

    @Test
    fun staticStringAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,          // "String"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x3F, 0xF3, 0xC1, 0xBD, 0xA5, 0x11, 0x9C, 0xE0,      // primitive(0x46, 0x08, 0x23, 0x98, 0x63, 0x99, 0x25, 0x24, 0x00, 0)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x67,                       // text(7)
                0x54, 0x45, 0x53, 0x54, 0x31, 0x32, 0x33        // "TEST0x12, 3"
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 1.2348)
        assert(attribute.value == "TEST123")
    }

    @Test
    fun parameterBooleanAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x69,                       // text(9)
                0x50, 0x61, 0x72, 0x61, 0x6D, 0x65, 0x74, 0x65, 0x72,    // "Parameter"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x42, 0x6F, 0x6F, 0x6C, 0x65, 0x61, 0x6E,        // "Boolean"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x3F, 0xF8, 0xDE, 0xD2, 0x88, 0xCE, 0x70, 0x3B,      // primitive(0x46, 0x09, 0x67, 0x92, 0x14, 0x43, 0x34, 0x31, 0x61, 1)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xF5                       // primitive(0x21, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 1.5544)
        assert(attribute.value == true)
    }

    @Test
    fun parameterIntegerAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x69,                       // text(9)
                0x50, 0x61, 0x72, 0x61, 0x6D, 0x65, 0x74, 0x65, 0x72,    // "Parameter"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x3F, 0xF8, 0xDA, 0x51, 0x19, 0xCE, 0x07, 0x5F,      // primitive(0x46, 0x09, 0x67, 0x42, 0x60, 0x47, 0x38, 0x41, 0x50, 3)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x19, 0x02, 0x9A                  // unsigned(0x66, 6)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 1.5533)
        assert(attribute.value == 666.toLong())
    }

    @Test
    fun parameterNumberAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x69,                       // text(9)
                0x50, 0x61, 0x72, 0x61, 0x6D, 0x65, 0x74, 0x65, 0x72,    // "Parameter"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,          // "Number"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x3F, 0xF8, 0xD5, 0xCF, 0xAA, 0xCD, 0x9E, 0x84,      // primitive(0x46, 0x09, 0x66, 0x93, 0x06, 0x51, 0x42, 0x51, 0x39, 6)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xFB, 0x40, 0x5E, 0xDD, 0x2F, 0x1A, 0x9F, 0xBE, 0x77      // primitive(0x46, 0x38, 0x38, 0x78, 0x60, 0x61, 0x80, 0x67, 0x57, 5)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 1.5522)
        assert(attribute.value == 123.456)
    }

    @Test
    fun parameterStringAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                            // map(4)
                0x6A,                         // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,    // "constraint"
                0x69,                         // text(9)
                0x50, 0x61, 0x72, 0x61, 0x6D, 0x65, 0x74, 0x65, 0x72,      // "Parameter"
                0x64,                         // text(4)
                0x74, 0x79, 0x70, 0x65,                // "type"
                0x66,                         // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,            // "String"
                0x69,                         // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,      // "timestamp"
                0xFB, 0x3F, 0xF8, 0xD1, 0x4E, 0x3B, 0xCD, 0x35, 0xA8,        // primitive(0x46, 0x09, 0x66, 0x43, 0x52, 0x55, 0x46, 0x61, 0x28, 8)
                0x65,                         // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,              // "value"
                0x6B,                         // text(0x11, )
                0x74, 0x65, 0x73, 0x74, 0x5F, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67  // "test_string"
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 1.5511)
        assert(attribute.value == "test_string")
    }

    @Test
    fun setPointBooleanAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x68,                       // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,      // "SetPoint"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x42, 0x6F, 0x6F, 0x6C, 0x65, 0x61, 0x6E,        // "Boolean"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x40, 0xC1, 0x5C, 0x47, 0x1A, 0x9F, 0xBE, 0x77,      // primitive(0x46, 0x66, 0x11, 0x21, 0x49, 0x39, 0x16, 0x54, 0x51, 9)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xF4                       // primitive(0x20, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 8888.5555)
        assert(attribute.value == false)
    }

    @Test
    fun setPointIntegerAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x68,                       // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,      // "SetPoint"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x40, 0xBE, 0x61, 0x71, 0xC4, 0x32, 0xCA, 0x58,      // primitive(0x46, 0x65, 0x27, 0x34, 0x05, 0x25, 0x32, 0x74, 0x20, 0)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x19, 0x07, 0xB9                  // unsigned(0x19, 0x77, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 7777.4444)
        assert(attribute.value == 1977.toLong())
    }

    @Test
    fun setPointNumberAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x68,                       // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,      // "SetPoint"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,          // "Number"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x40, 0xBA, 0x0A, 0x55, 0x53, 0x26, 0x17, 0xC2,      // primitive(0x46, 0x64, 0x05, 0x17, 0x25, 0x67, 0x90, 0x73, 0x21, 8)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xFB, 0x40, 0x09, 0x21, 0xCA, 0xC0, 0x83, 0x12, 0x6F      // primitive(0x46, 0x14, 0x25, 0x64, 0x47, 0x91, 0x47, 0x09, 0x61, 5)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 6666.3333)
        assert(attribute.value == 3.1415)
    }

    @Test
    fun setPointStringAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x68,                       // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,      // "SetPoint"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,          // "String"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x40, 0xB5, 0xB3, 0x38, 0xE2, 0x19, 0x65, 0x2C,      // primitive(0x46, 0x62, 0x83, 0x00, 0x46, 0x10, 0x48, 0x72, 0x23, 6)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x69,                       // text(9)
                0x61, 0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74    // "aSetPoint"
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 5555.2222)
        assert(attribute.value == "aSetPoint")
    }

    @Test
    fun statusBooleanAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,          // "Status"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x42, 0x6F, 0x6F, 0x6C, 0x65, 0x61, 0x6E,        // "Boolean"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x67, 0x8C, 0x29, 0xC7, 0x0A, 0x3D, 0x71,      // primitive(0x47, 0x12, 0x88, 0x96, 0x46, 0x12, 0x73, 0x98, 0x25, 7)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xF4                       // primitive(0x20, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 12345678.22)
        assert(attribute.value == false)
    }

    @Test
    fun statusIntegerAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,          // "Status"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x67, 0x8C, 0x29, 0xCA, 0x8F, 0x5C, 0x29,      // primitive(0x47, 0x12, 0x88, 0x96, 0x46, 0x18, 0x64, 0x54, 0x05, 7)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x38, 0x44                    // negative(0x68, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 12345678.33)
        assert(attribute.value == (-69).toLong())
    }

    @Test
    fun statusNumberAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,          // "Status"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,          // "Number"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x67, 0x8C, 0x29, 0xCE, 0x14, 0x7A, 0xE1,      // primitive(0x47, 0x12, 0x88, 0x96, 0x46, 0x24, 0x55, 0x09, 0x85, 7)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xFB, 0x40, 0x05, 0xBE, 0xDF, 0xA4, 0x3F, 0xE5, 0xC9      // primitive(0x46, 0x13, 0x30, 0x32, 0x61, 0x05, 0x35, 0x76, 0x64, 9)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 12345678.44)
        assert(attribute.value == 2.7182)
    }

    @Test
    fun statusStringAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x66,                       // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,          // "Status"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,          // "String"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x67, 0x8C, 0x29, 0xD1, 0x99, 0x99, 0x9A,      // primitive(0x47, 0x12, 0x88, 0x96, 0x46, 0x30, 0x45, 0x65, 0x65, 8)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x69,                       // text(9)
                0x4D, 0x79, 0x20, 0x53, 0x74, 0x61, 0x74, 0x75, 0x73    // "My Status"
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 12345678.55)
        assert(attribute.value == "My Status")
    }

    @Test
    fun measureBooleanAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x67,                       // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,        // "Measure"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x42, 0x6F, 0x6F, 0x6C, 0x65, 0x61, 0x6E,        // "Boolean"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x65, 0x68, 0x26, 0x03, 0xD7, 0x0A, 0x3D,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x59, 0x55, 0x62, 0x55, 7)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xF5                       // primitive(0x21, )
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 11223344.12)
        assert(attribute.value == true)
    }

    @Test
    fun measureIntegerAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x67,                       // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,        // "Measure"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x67,                       // text(7)
                0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x65, 0x68, 0x26, 0x0A, 0xE1, 0x47, 0xAE,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x71, 0x36, 0x74, 0x15, 8)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0x1A, 0x00, 0x01, 0x3A, 0x66              // unsigned(0x80, 0x48, 6)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 11223344.34)
        assert(attribute.value == 80486.toLong())
    }

    @Test
    fun measureNumberAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                          // map(4)
                0x6A,                       // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                0x67,                       // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,        // "Measure"
                0x64,                       // text(4)
                0x74, 0x79, 0x70, 0x65,              // "type"
                0x66,                       // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,          // "Number"
                0x69,                       // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                0xFB, 0x41, 0x65, 0x68, 0x26, 0x11, 0xEB, 0x85, 0x1F,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x83, 0x17, 0x85, 0x75, 9)
                0x65,                       // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                0xF9, 0xCD, 0xA0                  // primitive(0x52, 0x64, 0)
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 11223344.56)
        assert(attribute.value == -22.5f)
    }

    @Test
    fun measureStringAttributeDeserialize() {
        val attribute = CBORSerializationFormat().deserializeAttribute(arrayOf(
                0xA4,                            // map(4)
                0x6A,                         // text(0x10, )
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,    // "constraint"
                0x67,                         // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,          // "Measure"
                0x64,                         // text(4)
                0x74, 0x79, 0x70, 0x65,                // "type"
                0x66,                         // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,            // "String"
                0x69,                         // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,      // "timestamp"
                0xFB, 0x41, 0x65, 0x68, 0x26, 0x18, 0xF5, 0xC2, 0x8F,        // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x94, 0x98, 0x97, 0x35, 9)
                0x65,                         // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,              // "value"
                0x6B,                         // text(0x11, )
                0x6C, 0x6F, 0x72, 0x65, 0x6D, 0x20, 0x69, 0x70, 0x73, 0x75, 0x6D  // "lorem ipsum"
        ).map(Int::toByte).toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 11223344.78)
        assert(attribute.value == "lorem ipsum")
    }

    @Test
    fun nullValueAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x76, 0x61, 0x6C, 0x69, 0x64,        // "Invalid"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,          // "String"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0xFB, 0x41, 0x65, 0x68, 0x26, 0x18, 0xF5, 0xC2, 0x8F,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x94, 0x98, 0x97, 0x35, 9)
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0xF6                       // primitive(0x22, )
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun invalidConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                            // map(4)
                    0x6A,                         // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,    // "constraint"
                    0x67,                         // text(7)
                    0x49, 0x6E, 0x76, 0x61, 0x6C, 0x69, 0x64,          // "Invalid"
                    0x64,                         // text(4)
                    0x74, 0x79, 0x70, 0x65,                // "type"
                    0x66,                         // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,            // "String"
                    0x69,                         // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,      // "timestamp"
                    0xFB, 0x41, 0x65, 0x68, 0x26, 0x18, 0xF5, 0xC2, 0x8F,        // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x94, 0x98, 0x97, 0x35, 9)
                    0x65,                         // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,              // "value"
                    0x6B,                         // text(0x11, )
                    0x6C, 0x6F, 0x72, 0x65, 0x6D, 0x20, 0x69, 0x70, 0x73, 0x75, 0x6D  // "lorem ipsum"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonExistingConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x67,                       // text(7)
                    0x48, 0x6F, 0x74, 0x20, 0x44, 0x6F, 0x67,        // "Hot Dog"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0xFB, 0x41, 0x65, 0x68, 0x26, 0x18, 0xF5, 0xC2, 0x8F,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x94, 0x98, 0x97, 0x35, 9)
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun invalidTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x76, 0x61, 0x6C, 0x69, 0x64,        // "Invalid"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0xFB, 0x41, 0x65, 0x68, 0x26, 0x18, 0xF5, 0xC2, 0x8F,      // primitive(0x47, 0x12, 0x28, 0x70, 0x97, 0x94, 0x98, 0x97, 0x35, 9)
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonExistingTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                                  // map(4)
                    0x6A,                               // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,          // "constraint"
                    0x66,                               // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,                  // "Static"
                    0x64,                               // text(4)
                    0x74, 0x79, 0x70, 0x65,                      // "type"
                    0x69,                               // text(9)
                    0x53, 0x70, 0x61, 0x63, 0x65, 0x73, 0x68, 0x69, 0x70,            // "Spaceship"
                    0x69,                               // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,            // "timestamp"
                    0xFB, 0x40, 0x58, 0xC7, 0x2E, 0x48, 0xE8, 0xA7, 0x1E,              // primitive(0x46, 0x36, 0x67, 0x48, 0x17, 0x98, 0x35, 0x55, 0x35, 8)
                    0x65,                               // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                    // "value"
                    0x6E,                               // text(0x14, )
                    0x55, 0x53, 0x53, 0x20, 0x45, 0x6E, 0x74, 0x65, 0x72, 0x70, 0x72, 0x69, 0x73, 0x65  // "USS Enterprise"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun booleanTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0xF5,                       // primitive(0x21, )
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun stringTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0x65,                       // text(5)
                    0x54, 0x6F, 0x64, 0x61, 0x79,            // "Today"
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun negativeTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                          // map(4)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0x38, 0x4C,                    // negative(0x76, )
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonMatchingTypesAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA4,                                       // map(4)
                    0x6A,                                    // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,               // "constraint"
                    0x66,                                    // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,                       // "Static"
                    0x64,                                    // text(4)
                    0x74, 0x79, 0x70, 0x65,                           // "type"
                    0x67,                                    // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,                     // "Integer"
                    0x69,                                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                 // "timestamp"
                    0x18, 0x4D,                                 // unsigned(0x77, )
                    0x65,                                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                         // "value"
                    0x72,                                    // text(0x18, )
                    0x42, 0x75, 0x74, 0x20, 0x69, 0x74, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67  // "But it is a string"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA3,                        // map(3)
                    0x64,                     // text(4)
                    0x74, 0x79, 0x70, 0x65,            // "type"
                    0x67,                     // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,      // "Integer"
                    0x69,                     // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,  // "timestamp"
                    0x1A, 0x00, 0xBC, 0x61, 0x4E,            // unsigned(0x12, 0x34, 0x56, 0x78, )
                    0x65,                     // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,          // "value"
                    0x00                     // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA3,                          // map(3)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0x1A, 0x00, 0xBC, 0x61, 0x4E,              // unsigned(0x12, 0x34, 0x56, 0x78, )
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA3,                          // map(3)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00                       // unsigned(0)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingValueAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA3,                          // map(3)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0x1A, 0x00, 0xBC, 0x61, 0x4E              // unsigned(0x12, 0x34, 0x56, 0x78, )
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun additionalFieldAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(arrayOf(
                    0xA5,                          // map(5)
                    0x6A,                       // text(0x10, )
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,  // "constraint"
                    0x66,                       // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x69, 0x63,          // "Static"
                    0x64,                       // text(4)
                    0x74, 0x79, 0x70, 0x65,              // "type"
                    0x67,                       // text(7)
                    0x49, 0x6E, 0x74, 0x65, 0x67, 0x65, 0x72,        // "Integer"
                    0x69,                       // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,    // "timestamp"
                    0x1A, 0x00, 0xBC, 0x61, 0x4E,              // unsigned(0x12, 0x34, 0x56, 0x78, )
                    0x65,                       // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,            // "value"
                    0x00,                       // unsigned(0)
                    0x6A,                       // text(0x10, )
                    0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x69, 0x76, 0x65,  // "productive"
                    0xF5                       // primitive(0x21, )
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    /*
     * Transaction deserialization.
     */

    @Test
    fun validTransaction() {
        val transaction = CBORSerializationFormat().deserializeTransaction(arrayOf(
                0xA1,                                      // map(1)
                0x6A,                                   // text(0x10,)
                0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,              // "attributes"
                0xA2,                                   // map(2)
                0x78, 0x1A,                             // text(0x26,)
                0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                0xA4,                                // map(4)
                0x6A,                             // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                0x67,                             // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,              // "Measure"
                0x64,                             // text(4)
                0x74, 0x79, 0x70, 0x65,                    // "type"
                0x66,                             // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,                // "Number"
                0x69,                             // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                0x65,                             // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,            // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                0x78, 0x1F,                             // text(0x31,)
                0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                0xA4,                                // map(4)
                0x6A,                             // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                0x66,                             // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,                // "Status"
                0x64,                             // text(4)
                0x74, 0x79, 0x70, 0x65,                    // "type"
                0x66,                             // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,                // "String"
                0x69,                             // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                0x65,                             // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                0x67,                             // text(7)
                0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67              // "Running"
        ).map(Int::toByte).toByteArray())
        assert(transaction.attributes.count() == 2)
        transaction.attributes["aNode/anObject/anAttribute"]!!.apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.Number)
            assert(timestamp == 1598719840.3)
            assert(value == 37.22)
        }
        transaction.attributes["aNode/anObject/anotherAttribute"]!!.apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.String)
            assert(timestamp == 1598719840.5)
            assert(value == "Running")
        }
    }

    @Test
    fun additionalPropertyTransaction() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeTransaction(arrayOf(
                    0xA2,                                      // map(2)
                    0x6A,                                   // text(0x10,)
                    0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,              // "attributes"
                    0xA2,                                   // map(2)
                    0x78, 0x1A,                             // text(0x26,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                    0xA4,                                // map(4)
                    0x6A,                             // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                    0x67,                             // text(7)
                    0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,              // "Measure"
                    0x64,                             // text(4)
                    0x74, 0x79, 0x70, 0x65,                    // "type"
                    0x66,                             // text(6)
                    0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,                // "Number"
                    0x69,                             // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                             // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                    0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,            // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                    0x78, 0x1F,                             // text(0x31,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                    0xA4,                                // map(4)
                    0x6A,                             // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                    0x66,                             // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x75, 0x73,                // "Status"
                    0x64,                             // text(4)
                    0x74, 0x79, 0x70, 0x65,                    // "type"
                    0x66,                             // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,                // "String"
                    0x69,                             // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                    0x65,                             // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                    0x67,                             // text(7)
                    0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67,              // "Running"
                    0x64,                                   // text(4)
                    0x6D, 0x6F, 0x6F, 0x64,                          // "mood"
                    0x64,                                   // text(4)
                    0x67, 0x6F, 0x6F, 0x64                          // "good"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    @Test
    fun invalidAttributeTransaction() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeTransaction(arrayOf(
                    0xA1,                                      // map(1)
                    0x6A,                                   // text(0x10,)
                    0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,              // "attributes"
                    0xA2,                                   // map(2)
                    0x78, 0x1A,                             // text(0x26,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                    0xA4,                                // map(4)
                    0x6A,                             // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                    0x67,                             // text(7)
                    0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,              // "Measure"
                    0x64,                             // text(4)
                    0x74, 0x79, 0x70, 0x65,                    // "type"
                    0x66,                             // text(6)
                    0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,                // "Number"
                    0x69,                             // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                             // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                    0x65,                             // text(5)
                    0x33, 0x37, 0x2E, 0x32, 0x32,                  // "0x37,.0x22,"
                    0x78, 0x1F,                             // text(0x31,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                    0xA4,                                // map(4)
                    0x6A,                             // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,        // "constraint"
                    0x66,                             // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x75, 0x73,                // "Status"
                    0x64,                             // text(4)
                    0x74, 0x79, 0x70, 0x65,                    // "type"
                    0x66,                             // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,                // "String"
                    0x69,                             // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,          // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,            // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                    0x65,                             // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,                  // "value"
                    0x67,                             // text(7)
                    0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67              // "Running"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    @Test
    fun emptyTransaction() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeTransaction(arrayOf(0xA0).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    /*
     * Delayed messages deserialization.
     */

    @Test
    fun validDelayedMessagesDeserialize() {
        val delayedMessages = CBORSerializationFormat().deserializeDelayedMessages(arrayOf(
                0xA2,                                      // map(2)
                0x69,                                   // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                0xFB, 0x41, 0x67, 0x8C, 0x29, 0xD1, 0x99, 0x99, 0x9A,                  // primitive(0x47,0x12,0x88,0x96,0x46,0x30,0x45,0x65,0x65,8)
                0x68,                                   // text(8)
                0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x73,                  // "messages"
                0x84,                                   // array(4)
                0xA2,                                // map(2)
                0x65,                             // text(5)
                0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                0x78, 0x42,                          // text(0x66,)
                0x40, 0x75, 0x70, 0x64, 0x61, 0x74, 0x65, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@update.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                0x64,                             // text(4)
                0x64, 0x61, 0x74, 0x61,                    // "data"
                0xA4,                             // map(4)
                0x6A,                          // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                0x68,                          // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                0x64,                          // text(4)
                0x74, 0x79, 0x70, 0x65,                 // "type"
                0x66,                          // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                0x69,                          // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                0x65,                          // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                0x64,                          // text(4)
                0x54, 0x45, 0x53, 0x54,                 // "TEST"
                0xA2,                                // map(2)
                0x65,                             // text(5)
                0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                0x78, 0x31,                          // text(0x49,)
                0x40, 0x74, 0x72, 0x61, 0x6E, 0x73, 0x61, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@transaction.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                0x64,                             // text(4)
                0x64, 0x61, 0x74, 0x61,                    // "data"
                0xA1,                             // map(1)
                0x6A,                          // text(0x10,)
                0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,     // "attributes"
                0xA2,                          // map(2)
                0x78, 0x1A,                    // text(0x26,)
                0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                0xA4,                       // map(4)
                0x6A,                    // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                0x67,                    // text(7)
                0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,     // "Measure"
                0x64,                    // text(4)
                0x74, 0x79, 0x70, 0x65,           // "type"
                0x66,                    // text(6)
                0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,       // "Number"
                0x69,                    // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                0x65,                    // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,   // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                0x78, 0x1F,                    // text(0x31,)
                0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                0xA4,                       // map(4)
                0x6A,                    // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                0x66,                    // text(6)
                0x53, 0x74, 0x61, 0x74, 0x75, 0x73,       // "Status"
                0x64,                    // text(4)
                0x74, 0x79, 0x70, 0x65,           // "type"
                0x66,                    // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,       // "String"
                0x69,                    // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                0x65,                    // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                0x67,                    // text(7)
                0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67,     // "Running"
                0xA2,                                // map(2)
                0x65,                             // text(5)
                0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                0x78, 0x2A,                          // text(0x42,)
                0x40, 0x6C, 0x6F, 0x67, 0x73, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@logs.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                0x64,                             // text(4)
                0x64, 0x61, 0x74, 0x61,                    // "data"
                0xA5,                             // map(5)
                0x65,                          // text(5)
                0x6C, 0x65, 0x76, 0x65, 0x6C,               // "level"
                0x65,                          // text(5)
                0x44, 0x45, 0x42, 0x55, 0x47,               // "0xDE,BUG"
                0x69,                          // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                0xFB, 0x41, 0x93, 0x76, 0xC1, 0x5D, 0x8F, 0x5C, 0x29,         // primitive(0x47,0x25,0x25,0x10,0x06,0x91,0x22,0x24,0x29,7)
                0x67,                          // text(7)
                0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,           // "message"
                0x6C,                          // text(0x12,)
                0x54, 0x65, 0x73, 0x74, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Test message"
                0x6A,                          // text(0x10,)
                0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,     // "loggerName"
                0x6B,                          // text(0x11,)
                0x6D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,   // "main logger"
                0x69,                          // text(9)
                0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,       // "logSource"
                0x68,                          // text(8)
                0x65, 0x6E, 0x64, 0x70, 0x6F, 0x69, 0x6E, 0x74,         // "endpoint"
                0xA2,                                // map(2)
                0x65,                             // text(5)
                0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                0x78, 0x42,                          // text(0x66,)
                0x40, 0x64, 0x69, 0x64, 0x53, 0x65, 0x74, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@didSet.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                0x64,                             // text(4)
                0x64, 0x61, 0x74, 0x61,                    // "data"
                0xA4,                             // map(4)
                0x6A,                          // text(0x10,)
                0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                0x68,                          // text(8)
                0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                0x64,                          // text(4)
                0x74, 0x79, 0x70, 0x65,                 // "type"
                0x66,                          // text(6)
                0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                0x69,                          // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                0x65,                          // text(5)
                0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                0x64,                          // text(4)
                0x54, 0x45, 0x53, 0x54                 // "TEST"
        ).map(Int::toByte).toByteArray())
        assert(delayedMessages.timestamp == 12345678.55)
        assert(delayedMessages.messages.count() == 4)

        delayedMessages.messages[0].apply {
            assert(topic == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute")
            assert(data is Attribute)
            (data as Attribute).apply {
                assert(constraint == AttributeConstraint.SetPoint)
                assert(type == AttributeType.String)
                assert(timestamp == 1598719840.3)
                assert(value == "TEST")
            }
        }
        delayedMessages.messages[1].apply {
            assert(topic == "@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
            assert(data is Transaction)
            (data as Transaction).apply {
                // TODO...
            }
        }
        delayedMessages.messages[2].apply {
            assert(topic == "@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
            assert(data is LogMessage)
            (data as LogMessage).apply {
                // TODO...
            }
        }
        delayedMessages.messages[3].apply {
            assert(topic == "@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute")
            assert(data is Attribute)
            (data as Attribute).apply {
                assert(constraint == AttributeConstraint.SetPoint)
                assert(type == AttributeType.String)
                assert(timestamp == 1598719840.3)
                assert(value == "TEST")
            }
        }
    }

    @Test
    fun notAllowedDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeDelayedMessages(arrayOf(
                    0xA2,                                      // map(2)
                    0x69,                                   // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                    0xFB, 0x41, 0x67, 0x8C, 0x29, 0xD1, 0x99, 0x99, 0x9A,                  // primitive(0x47,0x12,0x88,0x96,0x46,0x30,0x45,0x65,0x65,8)
                    0x68,                                   // text(8)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x73,                  // "messages"
                    0x85,                                   // array(5)
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x75, 0x70, 0x64, 0x61, 0x74, 0x65, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@update.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54,                 // "TEST"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x31,                          // text(0x49,)
                    0x40, 0x74, 0x72, 0x61, 0x6E, 0x73, 0x61, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@transaction.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA1,                             // map(1)
                    0x6A,                          // text(0x10,)
                    0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,     // "attributes"
                    0xA2,                          // map(2)
                    0x78, 0x1A,                    // text(0x26,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x67,                    // text(7)
                    0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,     // "Measure"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,       // "Number"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,   // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                    0x78, 0x1F,                    // text(0x31,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x75, 0x73,       // "Status"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,       // "String"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0x67,                    // text(7)
                    0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67,     // "Running"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x2A,                          // text(0x42,)
                    0x40, 0x6C, 0x6F, 0x67, 0x73, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@logs.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA5,                             // map(5)
                    0x65,                          // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,               // "level"
                    0x65,                          // text(5)
                    0x44, 0x45, 0x42, 0x55, 0x47,               // "0xDE,BUG"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0x93, 0x76, 0xC1, 0x5D, 0x8F, 0x5C, 0x29,         // primitive(0x47,0x25,0x25,0x10,0x06,0x91,0x22,0x24,0x29,7)
                    0x67,                          // text(7)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,           // "message"
                    0x6C,                          // text(0x12,)
                    0x54, 0x65, 0x73, 0x74, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Test message"
                    0x6A,                          // text(0x10,)
                    0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,     // "loggerName"
                    0x6B,                          // text(0x11,)
                    0x6D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,   // "main logger"
                    0x69,                          // text(9)
                    0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,       // "logSource"
                    0x68,                          // text(8)
                    0x65, 0x6E, 0x64, 0x70, 0x6F, 0x69, 0x6E, 0x74,         // "endpoint"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x64, 0x69, 0x64, 0x53, 0x65, 0x74, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@didSet.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54,                 // "TEST"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x3F,                          // text(0x63,)
                    0x40, 0x73, 0x65, 0x74, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@set.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54                 // "TEST"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun missingTimestampDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeDelayedMessages(arrayOf(
                    0xA1,                                      // map(1)
                    0x68,                                   // text(8)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x73,                  // "messages"
                    0x84,                                   // array(4)
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x75, 0x70, 0x64, 0x61, 0x74, 0x65, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@update.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54,                 // "TEST"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x31,                          // text(0x49,)
                    0x40, 0x74, 0x72, 0x61, 0x6E, 0x73, 0x61, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@transaction.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA1,                             // map(1)
                    0x6A,                          // text(0x10,)
                    0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,     // "attributes"
                    0xA2,                          // map(2)
                    0x78, 0x1A,                    // text(0x26,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x67,                    // text(7)
                    0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,     // "Measure"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,       // "Number"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,   // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                    0x78, 0x1F,                    // text(0x31,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x75, 0x73,       // "Status"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,       // "String"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0x67,                    // text(7)
                    0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67,     // "Running"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x2A,                          // text(0x42,)
                    0x40, 0x6C, 0x6F, 0x67, 0x73, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@logs.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA5,                             // map(5)
                    0x65,                          // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,               // "level"
                    0x65,                          // text(5)
                    0x44, 0x45, 0x42, 0x55, 0x47,               // "0xDE,BUG"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0x93, 0x76, 0xC1, 0x5D, 0x8F, 0x5C, 0x29,         // primitive(0x47,0x25,0x25,0x10,0x06,0x91,0x22,0x24,0x29,7)
                    0x67,                          // text(7)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,           // "message"
                    0x6C,                          // text(0x12,)
                    0x54, 0x65, 0x73, 0x74, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Test message"
                    0x6A,                          // text(0x10,)
                    0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,     // "loggerName"
                    0x6B,                          // text(0x11,)
                    0x6D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,   // "main logger"
                    0x69,                          // text(9)
                    0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,       // "logSource"
                    0x68,                          // text(8)
                    0x65, 0x6E, 0x64, 0x70, 0x6F, 0x69, 0x6E, 0x74,         // "endpoint"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x64, 0x69, 0x64, 0x53, 0x65, 0x74, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@didSet.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54                 // "TEST"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun missingMessagesDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages(arrayOf(
                    0xA1,                       // map(1)
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0x67, 0x8C, 0x29, 0xD1, 0x99, 0x99, 0x9A   // primitive(0x47,0x12,0x88,0x96,0x46,0x30,0x45,0x65,0x65,8)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun additionalPropertyDelayedMessagesDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages(arrayOf(
                    0xA3,                                      // map(3)
                    0x67,                                   // text(7)
                    0x77, 0x65, 0x61, 0x74, 0x68, 0x65, 0x72,                    // "weather"
                    0x65,                                   // text(5)
                    0x63, 0x6C, 0x65, 0x61, 0x72,                        // "clear"
                    0x69,                                   // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                    0xFB, 0x41, 0x67, 0x8C, 0x29, 0xD1, 0x99, 0x99, 0x9A,                  // primitive(0x47,0x12,0x88,0x96,0x46,0x30,0x45,0x65,0x65,8)
                    0x68,                                   // text(8)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x73,                  // "messages"
                    0x84,                                   // array(4)
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x75, 0x70, 0x64, 0x61, 0x74, 0x65, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@update.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54,                 // "TEST"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x31,                          // text(0x49,)
                    0x40, 0x74, 0x72, 0x61, 0x6E, 0x73, 0x61, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@transaction.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA1,                             // map(1)
                    0x6A,                          // text(0x10,)
                    0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73,     // "attributes"
                    0xA2,                          // map(2)
                    0x78, 0x1A,                    // text(0x26,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x67,                    // text(7)
                    0x4D, 0x65, 0x61, 0x73, 0x75, 0x72, 0x65,     // "Measure"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72,       // "Number"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0xFB, 0x40, 0x42, 0x9C, 0x28, 0xF5, 0xC2, 0x8F, 0x5C,   // primitive(0x46,0x30,0x43,0x50,0x66,0x62,0x60,0x84,0x70,0)
                    0x78, 0x1F,                    // text(0x31,)
                    0x61, 0x4E, 0x6F, 0x64, 0x65, 0x2F, 0x61, 0x6E, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2F, 0x61, 0x6E, 0x6F, 0x74, 0x68, 0x65, 0x72, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "aNode/anObject/anotherAttribute"
                    0xA4,                       // map(4)
                    0x6A,                    // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74, // "constraint"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x61, 0x74, 0x75, 0x73,       // "Status"
                    0x64,                    // text(4)
                    0x74, 0x79, 0x70, 0x65,           // "type"
                    0x66,                    // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,       // "String"
                    0x69,                    // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70, // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x20, 0x00, 0x00,   // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x56,0x50,0x30,4)
                    0x65,                    // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,         // "value"
                    0x67,                    // text(7)
                    0x52, 0x75, 0x6E, 0x6E, 0x69, 0x6E, 0x67,     // "Running"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x2A,                          // text(0x42,)
                    0x40, 0x6C, 0x6F, 0x67, 0x73, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, // "@logs.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA5,                             // map(5)
                    0x65,                          // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,               // "level"
                    0x65,                          // text(5)
                    0x44, 0x45, 0x42, 0x55, 0x47,               // "0xDE,BUG"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0x93, 0x76, 0xC1, 0x5D, 0x8F, 0x5C, 0x29,         // primitive(0x47,0x25,0x25,0x10,0x06,0x91,0x22,0x24,0x29,7)
                    0x67,                          // text(7)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,           // "message"
                    0x6C,                          // text(0x12,)
                    0x54, 0x65, 0x73, 0x74, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Test message"
                    0x6A,                          // text(0x10,)
                    0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,     // "loggerName"
                    0x6B,                          // text(0x11,)
                    0x6D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,   // "main logger"
                    0x69,                          // text(9)
                    0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,       // "logSource"
                    0x68,                          // text(8)
                    0x65, 0x6E, 0x64, 0x70, 0x6F, 0x69, 0x6E, 0x74,         // "endpoint"
                    0xA2,                                // map(2)
                    0x65,                             // text(5)
                    0x74, 0x6F, 0x70, 0x69, 0x63,                  // "topic"
                    0x78, 0x42,                          // text(0x66,)
                    0x40, 0x64, 0x69, 0x64, 0x53, 0x65, 0x74, 0x2E, 0x63, 0x37, 0x62, 0x66, 0x61, 0x61, 0x31, 0x63, 0x2D, 0x38, 0x35, 0x37, 0x66, 0x2D, 0x34, 0x33, 0x38, 0x61, 0x2D, 0x62, 0x35, 0x66, 0x30, 0x2D, 0x34, 0x34, 0x37, 0x65, 0x33, 0x63, 0x64, 0x33, 0x34, 0x66, 0x36, 0x36, 0x2E, 0x6E, 0x6F, 0x64, 0x65, 0x2E, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x2E, 0x61, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, // "@didSet.c7bfaa1c-0x85,7f-0x43,8a-b5f0-0x44,7e3cd0x34,f0x66,.node.object.attribute"
                    0x64,                             // text(4)
                    0x64, 0x61, 0x74, 0x61,                    // "data"
                    0xA4,                             // map(4)
                    0x6A,                          // text(0x10,)
                    0x63, 0x6F, 0x6E, 0x73, 0x74, 0x72, 0x61, 0x69, 0x6E, 0x74,     // "constraint"
                    0x68,                          // text(8)
                    0x53, 0x65, 0x74, 0x50, 0x6F, 0x69, 0x6E, 0x74,         // "SetPoint"
                    0x64,                          // text(4)
                    0x74, 0x79, 0x70, 0x65,                 // "type"
                    0x66,                          // text(6)
                    0x53, 0x74, 0x72, 0x69, 0x6E, 0x67,             // "String"
                    0x69,                          // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,       // "timestamp"
                    0xFB, 0x41, 0xD7, 0xD2, 0xA1, 0xD8, 0x13, 0x33, 0x33,         // primitive(0x47,0x44,0x49,0x23,0x25,0x01,0x48,0x11,0x44,3)
                    0x65,                          // text(5)
                    0x76, 0x61, 0x6C, 0x75, 0x65,               // "value"
                    0x64,                          // text(4)
                    0x54, 0x45, 0x53, 0x54                 // "TEST"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun emptyDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages(arrayOf(0xA0).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    /*
    * Log message deserialization.
    */

    @Test
    fun minimalLogMessageDeserialize() {
        val logMessage = CBORSerializationFormat().deserializeLogMessage(arrayOf(
                0xA3,                                      // map(3)
                0x65,                                   // text(5)
                0x6C, 0x65, 0x76, 0x65, 0x6C,                        // "level"
                0x64,                                   // text(4)
                0x49, 0x4E, 0x46, 0x4F,                          // "INFO"
                0x69,                                   // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                0x6C,                                   // text(0x12,)
                0x31, 0x35, 0x39, 0x39, 0x30, 0x36, 0x34, 0x36, 0x34, 0x38, 0x2E, 0x38,          // "0x15,0x99,0x06,0x46,0x48,.8"
                0x67,                                   // text(7)
                0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,                    // "message"
                0x73,                                   // text(0x19,)
                0x4D, 0x69, 0x6E, 0x69, 0x6D, 0x61, 0x6C, 0x20, 0x6C, 0x6F, 0x67, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65 // "Minimal log message"
        ).map(Int::toByte).toByteArray())
        assert(logMessage.level == LogLevel.INFO)
        assert(logMessage.timestamp == 1599064648.8)
        assert(logMessage.message == "Minimal log message")
    }

    @Test
    fun completeLogMessageDeserialize() {
        val logMessage = CBORSerializationFormat().deserializeLogMessage(arrayOf(
                0xA5,                                      // map(5)
                0x65,                                   // text(5)
                0x6C, 0x65, 0x76, 0x65, 0x6C,                        // "level"
                0x64,                                   // text(4)
                0x49, 0x4E, 0x46, 0x4F,                          // "INFO"
                0x69,                                   // text(9)
                0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                0x6C,                                   // text(0x12,)
                0x31, 0x35, 0x39, 0x39, 0x30, 0x36, 0x34, 0x36, 0x34, 0x38, 0x2E, 0x38,          // "0x15,0x99,0x06,0x46,0x48,.8"
                0x67,                                   // text(7)
                0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,                    // "message"
                0x73,                                   // text(0x19,)
                0x4D, 0x69, 0x6E, 0x69, 0x6D, 0x61, 0x6C, 0x20, 0x6C, 0x6F, 0x67, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Minimal log message"
                0x6A,                                   // text(0x10,)
                0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,              // "loggerName"
                0x6B,                                   // text(0x11,)
                0x4D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,            // "Main logger"
                0x69,                                   // text(9)
                0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,                // "logSource"
                0x69,                                   // text(9)
                0x54, 0x68, 0x72, 0x65, 0x61, 0x64, 0x20, 0x32, 0x32                // "Thread 0x22,"
        ).map(Int::toByte).toByteArray())
        assert(logMessage.level == LogLevel.INFO)
        assert(logMessage.timestamp == 1599064648.8)
        assert(logMessage.message == "Minimal log message")
        assert(logMessage.loggerName == "Main logger")
        assert(logMessage.logSource == "Thread 22")
    }

    @Test
    fun invalidLevelLogMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeLogMessage(arrayOf(
                    0xA5,                                      // map(5)
                    0x65,                                   // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,                        // "level"
                    0x67,                                   // text(7)
                    0x57, 0x48, 0x49, 0x53, 0x50, 0x45, 0x52,                    // "WHISPER"
                    0x69,                                   // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                    0x6C,                                   // text(0x12,)
                    0x31, 0x35, 0x39, 0x39, 0x30, 0x36, 0x34, 0x36, 0x34, 0x38, 0x2E, 0x38,          // "0x15,0x99,0x06,0x46,0x48,.8"
                    0x67,                                   // text(7)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,                    // "message"
                    0x73,                                   // text(0x19,)
                    0x4D, 0x69, 0x6E, 0x69, 0x6D, 0x61, 0x6C, 0x20, 0x6C, 0x6F, 0x67, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Minimal log message"
                    0x6A,                                   // text(0x10,)
                    0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,              // "loggerName"
                    0x6B,                                   // text(0x11,)
                    0x4D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,            // "Main logger"
                    0x69,                                   // text(9)
                    0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,                // "logSource"
                    0x69,                                   // text(9)
                    0x54, 0x68, 0x72, 0x65, 0x61, 0x64, 0x20, 0x32, 0x32                // "Thread 0x22,"
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing log message.")
    }

    @Test
    fun additionalPropertyLogMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeLogMessage(arrayOf(
                    0xA6,                                      // map(6)
                    0x65,                                   // text(5)
                    0x6C, 0x65, 0x76, 0x65, 0x6C,                        // "level"
                    0x65,                                   // text(5)
                    0x44, 0x45, 0x42, 0x55, 0x47,                        // "0xDE,BUG"
                    0x69,                                   // text(9)
                    0x74, 0x69, 0x6D, 0x65, 0x73, 0x74, 0x61, 0x6D, 0x70,                // "timestamp"
                    0x6C,                                   // text(0x12,)
                    0x31, 0x35, 0x39, 0x39, 0x30, 0x36, 0x34, 0x36, 0x34, 0x38, 0x2E, 0x38,          // "0x15,0x99,0x06,0x46,0x48,.8"
                    0x67,                                   // text(7)
                    0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,                    // "message"
                    0x73,                                   // text(0x19,)
                    0x4D, 0x69, 0x6E, 0x69, 0x6D, 0x61, 0x6C, 0x20, 0x6C, 0x6F, 0x67, 0x20, 0x6D, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, // "Minimal log message"
                    0x6A,                                   // text(0x10,)
                    0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72, 0x4E, 0x61, 0x6D, 0x65,              // "loggerName"
                    0x6B,                                   // text(0x11,)
                    0x4D, 0x61, 0x69, 0x6E, 0x20, 0x6C, 0x6F, 0x67, 0x67, 0x65, 0x72,            // "Main logger"
                    0x69,                                   // text(9)
                    0x6C, 0x6F, 0x67, 0x53, 0x6F, 0x75, 0x72, 0x63, 0x65,                // "logSource"
                    0x69,                                   // text(9)
                    0x54, 0x68, 0x72, 0x65, 0x61, 0x64, 0x20, 0x32, 0x32,                // "Thread 0x22,"
                    0x64,                                   // text(4)
                    0x74, 0x6F, 0x74, 0x6F,                          // "toto"
                    0xF4                                   // primitive(0x20,)
            ).map(Int::toByte).toByteArray())
        }
        assert(exception.message == "Error deserializing log message.")
    }
}

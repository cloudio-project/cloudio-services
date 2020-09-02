package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import org.junit.Assert.assertThrows
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.nio.charset.StandardCharsets

class JSONSerializationFormatTests {

    /*
     * Endpoint data model deserialization.
     */

    @Test
    fun endpointDataModelNoNodesDeserialize() {
        val endpointDataModel = JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.2",
                "supportedFormats": [ "JSON", "CBOR" ],
                "nodes": {}
            }
        """.trimIndent().toByteArray())
        assert(endpointDataModel.version == "v0.2")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelThreeNodesDeserialize() {
        val endpointDataModel = JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.2",
                "supportedFormats": [ "JSON", "CBOR" ],
                "nodes": {
                    "one": {
                        "implements": [],
                        "objects": {}
                    },
                    "two": {
                        "implements": [],
                        "objects": {}
                    },
                    "three": {
                        "implements": [],
                        "objects": {}
                    }
                }
            }
        """.trimIndent().toByteArray())
        assert(endpointDataModel.version == "v0.2")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.count() == 3)
    }

    @Test
    fun endpointDataModelNoVersionDeserialize() {
        val endpointDataModel = JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "supportedFormats": [ "JSON", "CBOR" ],
                "nodes": {}
            }
        """.trimIndent().toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON", "CBOR"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelNoVersionNoSupportedFormatsDeserialize() {
        val endpointDataModel = JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "nodes": {}
            }
        """.trimIndent().toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelNoNodesPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.2",
                "supportedFormats": [ "JSON", "CBOR" ]
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelV1NoSupportedFormatsDeserialize() {
        val endpointDataModel = JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.1",
                "nodes": {}
            }
        """.trimIndent().toByteArray())
        assert(endpointDataModel.version == "v0.1")
        assert(endpointDataModel.supportedFormats == setOf("JSON"))
        assert(endpointDataModel.nodes.isEmpty())
    }

    @Test
    fun endpointDataModelEmptyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeEndpointDataModel("""
            {
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelV2NoSupportedFormatsDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.2",
                "nodes": {}
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    @Test
    fun endpointDataModelAdditionalPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeEndpointDataModel("""
            {
                "version": "v0.2",
                "supportedFormats": [ "JSON", "CBOR" ],
                "nodes": {},
                "is_fun": true
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing endpoint data model.")
    }

    /*
     * Node deserialization.
     */

    @Test
    fun nodeNoObjectsDeserialize() {
        val node = JSONSerializationFormat().deserializeNode("""
            {
                "implements": ["InterfaceA", "InterfaceB"],
                "objects": {}
            }
        """.trimIndent().toByteArray())
        assert(!node.online)
        assert(node.implements.count() == 2 && node.implements == setOf("InterfaceA", "InterfaceB"))
        assert(node.objects.count() == 0)
    }

    @Test
    fun nodeTwoObjectsDeserialize() {
        val node = JSONSerializationFormat().deserializeNode("""
            {
                "implements": ["InterfaceA", "InterfaceB"],
                "objects": {
                    "obj1": {
                        "conforms": null,
                        "objects": {},
                        "attributes": {}
                    },
                    "obj2": {
                        "conforms": null,
                        "objects": {},
                        "attributes": {}
                    }
                }
            }
        """.trimIndent().toByteArray())
        assert(!node.online)
        assert(node.implements.count() == 2 && node.implements == setOf("InterfaceA", "InterfaceB"))
        assert(node.objects.count() == 2)
    }

    @Test
    fun nodeNoImplementsDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeNode("""
            {
                "objects": {}
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    @Test
    fun nodeNoObjectsPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeNode("""
            {
                "implements": ["InterfaceA", "InterfaceB"]
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    @Test
    fun nodeUnknownPropertyDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeNode("""
            {
                "level": "44",
                "implements": ["InterfaceA", "InterfaceB"],
                "objects": {
                    "obj1": {},
                    "obj2": {}
                }
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing node.")
    }

    /*
     * Attribute serialization.
     */

    @Test
    fun staticBooleanAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Boolean,
                timestamp = 1.2345,
                value = false
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Static",
                "type": "Boolean",
                "timestamp": 1.2345,
                "value": false
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun staticIntegerAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Integer,
                timestamp = 1.2346,
                value = 42
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": 1.2346,
                "value": 42
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun staticNumberAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.Number,
                timestamp = 1.2347,
                value = 42.24
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Static",
                "type": "Number",
                "timestamp": 1.2347,
                "value": 42.24
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun staticStringAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Static,
                type = AttributeType.String,
                timestamp = 1.2348,
                value = "TEST123"
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Static",
                "type": "String",
                "timestamp": 1.2348,
                "value": "TEST123"
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun parameterBooleanAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Boolean,
                timestamp = 1.5533,
                value = true
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Parameter",
                "type": "Boolean",
                "timestamp": 1.5533,
                "value": true
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun parameterIntegerAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Integer,
                timestamp = 1.5544,
                value = 666
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Parameter",
                "type": "Integer",
                "timestamp": 1.5544,
                "value": 666
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun parameterNumberAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.Number,
                timestamp = 1.5533,
                value = 123.456
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Parameter",
                "type": "Number",
                "timestamp": 1.5533,
                "value": 123.456
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun parameterStringAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Parameter,
                type = AttributeType.String,
                timestamp = 1.5522,
                value = "test_string"
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Parameter",
                "type": "String",
                "timestamp": 1.5522,
                "value": "test_string"
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun setPointBooleanAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Boolean,
                timestamp = 8888.5555,
                value = false
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "SetPoint",
                "type": "Boolean",
                "timestamp": 8888.5555,
                "value": false
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun setPointIntegerAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Integer,
                timestamp = 7777.4444,
                value = 1977
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "SetPoint",
                "type": "Integer",
                "timestamp": 7777.4444,
                "value": 1977
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun setPointNumberAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.Number,
                timestamp = 6666.3333,
                value = 3.1415
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "SetPoint",
                "type": "Number",
                "timestamp": 6666.3333,
                "value": 3.1415
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun setPointStringAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.SetPoint,
                type = AttributeType.String,
                timestamp = 5555.2222,
                value = "aSetPoint"
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "SetPoint",
                "type": "String",
                "timestamp": 5555.2222,
                "value": "aSetPoint"
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun statusBooleanAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Boolean,
                timestamp = 12345678.22,
                value = true
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Status",
                "type": "Boolean",
                "timestamp": 12345678.22,
                "value": true
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun statusIntegerAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Integer,
                timestamp = 12345678.33,
                value = -69
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Status",
                "type": "Integer",
                "timestamp": 12345678.33,
                "value": -69
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun statusNumberAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.Number,
                timestamp = 12345678.44,
                value = 2.7182
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Status",
                "type": "Number",
                "timestamp": 12345678.44,
                "value": 2.7182
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun statusStringAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Status,
                type = AttributeType.String,
                timestamp = 12345678.55,
                value = "My Status"
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Status",
                "type": "String",
                "timestamp": 12345678.55,
                "value": "My Status"
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun measureBooleanAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Boolean,
                timestamp = 11223344.22,
                value = false
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Measure",
                "type": "Boolean",
                "timestamp": 11223344.22,
                "value": false
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun measureIntegerAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Integer,
                timestamp = 11223344.33,
                value = 80486
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Measure",
                "type": "Integer",
                "timestamp": 11223344.33,
                "value": 80486
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun measureNumberAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.Number,
                timestamp = 11223344.44,
                value = -22.5
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Measure",
                "type": "Number",
                "timestamp": 11223344.44,
                "value": -22.5
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun measureStringAttributeSerialize() {
        val json = JSONSerializationFormat().serializeAttribute(Attribute(
                constraint = AttributeConstraint.Measure,
                type = AttributeType.String,
                timestamp = 11223344.55,
                value = "lorem ipsum"
        ))
        JSONAssert.assertEquals("""
            {
                "constraint": "Measure",
                "type": "String",
                "timestamp": 11223344.55,
                "value": "lorem ipsum"
            }
        """.trimIndent(), String(json, StandardCharsets.UTF_8), false)
    }

    @Test
    fun invalidConstraintAttributeSerialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().serializeAttribute(Attribute(
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
            JSONSerializationFormat().serializeAttribute(Attribute(
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
            JSONSerializationFormat().serializeAttribute(Attribute(
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
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Boolean",
                "timestamp": 1.2345,
                "value": false
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 1.2345)
        assert(attribute.value == false)
    }

    @Test
    fun staticIntegerAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": 1.2346,
                "value": 42
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 1.2346)
        assert(attribute.value == 42.toLong())
    }

    @Test
    fun staticNumberAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Number",
                "timestamp": 1.2347,
                "value": 42.24
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 1.2347)
        assert(attribute.value == 42.24)
    }

    @Test
    fun staticStringAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "String",
                "timestamp": 1.2348,
                "value": "TEST123"
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Static)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 1.2348)
        assert(attribute.value == "TEST123")
    }

    @Test
    fun parameterBooleanAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Parameter",
                "type": "Boolean",
                "timestamp": 1.5544,
                "value": true
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 1.5544)
        assert(attribute.value == true)
    }

    @Test
    fun parameterIntegerAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Parameter",
                "type": "Integer",
                "timestamp": 1.5533,
                "value": 666
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 1.5533)
        assert(attribute.value == 666.toLong())
    }

    @Test
    fun parameterNumberAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Parameter",
                "type": "Number",
                "timestamp": 1.5522,
                "value": 123.456
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 1.5522)
        assert(attribute.value == 123.456)
    }

    @Test
    fun parameterStringAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Parameter",
                "type": "String",
                "timestamp": 1.5511,
                "value": "test_string"
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Parameter)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 1.5511)
        assert(attribute.value == "test_string")
    }

    @Test
    fun setPointBooleanAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "SetPoint",
                "type": "Boolean",
                "timestamp": 8888.5555,
                "value": false
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 8888.5555)
        assert(attribute.value == false)
    }

    @Test
    fun setPointIntegerAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "SetPoint",
                "type": "Integer",
                "timestamp": 7777.4444,
                "value": 1977
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 7777.4444)
        assert(attribute.value == 1977.toLong())
    }

    @Test
    fun setPointNumberAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "SetPoint",
                "type": "Number",
                "timestamp": 6666.3333,
                "value": 3.1415
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 6666.3333)
        assert(attribute.value == 3.1415)
    }

    @Test
    fun setPointStringAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "SetPoint",
                "type": "String",
                "timestamp": 5555.2222,
                "value": "aSetPoint"
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.SetPoint)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 5555.2222)
        assert(attribute.value == "aSetPoint")
    }

    @Test
    fun statusBooleanAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Status",
                "type": "Boolean",
                "timestamp": 12345678.22,
                "value": false
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 12345678.22)
        assert(attribute.value == false)
    }

    @Test
    fun statusIntegerAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Status",
                "type": "Integer",
                "timestamp": 12345678.33,
                "value": -69
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 12345678.33)
        assert(attribute.value == (-69).toLong())
    }

    @Test
    fun statusNumberAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Status",
                "type": "Number",
                "timestamp": 12345678.44,
                "value": 2.7182
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 12345678.44)
        assert(attribute.value == 2.7182)
    }

    @Test
    fun statusStringAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Status",
                "type": "String",
                "timestamp": 12345678.55,
                "value": "My Status"
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Status)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 12345678.55)
        assert(attribute.value == "My Status")
    }

    @Test
    fun measureBooleanAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Measure",
                "type": "Boolean",
                "timestamp": 11223344.12,
                "value": true
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Boolean)
        assert(attribute.timestamp == 11223344.12)
        assert(attribute.value == true)
    }

    @Test
    fun measureIntegerAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Measure",
                "type": "Integer",
                "timestamp": 11223344.34,
                "value": 80486
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Integer)
        assert(attribute.timestamp == 11223344.34)
        assert(attribute.value == 80486.toLong())
    }

    @Test
    fun measureNumberAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Measure",
                "type": "Number",
                "timestamp": 11223344.56,
                "value": -22.5
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.Number)
        assert(attribute.timestamp == 11223344.56)
        assert(attribute.value == -22.5)
    }

    @Test
    fun measureStringAttributeDeserialize() {
        val attribute = JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Measure",
                "type": "String",
                "timestamp": 11223344.78,
                "value": "lorem ipsum"
            }
        """.trimIndent().toByteArray())
        assert(attribute.constraint == AttributeConstraint.Measure)
        assert(attribute.type == AttributeType.String)
        assert(attribute.timestamp == 11223344.78)
        assert(attribute.value == "lorem ipsum")
    }

    @Test
    fun nullValueAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Invalid",
                "type": "String",
                "timestamp": 11223344.78,
                "value": null
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun invalidConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Invalid",
                "type": "String",
                "timestamp": 11223344.78,
                "value": "lorem ipsum"
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonExistingConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Hot Dog",
                "type": "Integer",
                "timestamp": 11223344.78,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun invalidTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Invalid",
                "timestamp": 11223344.78,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonExistingTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Spaceship",
                "timestamp": 99.1122,
                "value": "USS Enterprise"
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun booleanTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": true,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun stringTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": "Today",
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun negativeTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": -77,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun nonMatchingTypesAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": 77,
                "value": "But it is a string"
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingConstraintAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "type": "Integer",
                "timestamp": 12345678,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingTypeAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "timestamp": 12345678,
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingTimestampAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "value": 0
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun missingValueAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": 12345678
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    @Test
    fun additionalFieldAttributeDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute("""
            {
                "constraint": "Static",
                "type": "Integer",
                "timestamp": 12345678,
                "value": 0,
                "productive": true
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing attribute.")
    }

    /*
     * Transaction deserialization.
     */

    @Test
    fun validTransactionDeserialize() {
        val transaction = JSONSerializationFormat().deserializeTransaction("""
            {
                "attributes": {
                    "aNode/anObject/anAttribute": {
                        "constraint": "Measure",
                        "type": "Number",
                        "timestamp": 1598719840.3,
                        "value": 37.22
                    },
                    "aNode/anObject/anotherAttribute": {
                        "constraint": "Status",
                        "type": "String",
                        "timestamp": 1598719840.5,
                        "value": "Running"
                    }
                }
            }
        """.trimIndent().toByteArray())
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
    fun additionalPropertyTransactionDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeTransaction("""
            {
                "attributes": {
                    "aNode/anObject/anAttribute": {
                        "constraint": "Measure",
                        "type": "Number",
                        "timestamp": 1598719840.3,
                        "value": 37.22
                    },
                    "aNode/anObject/anotherAttribute": {
                        "constraint": "Status",
                        "type": "String",
                        "timestamp": 1598719840.5,
                        "value": "Running"
                    }
                },
                "mood": "good"
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    @Test
    fun invalidAttributeTransactionDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeTransaction("""
            {
                "attributes": {
                    "aNode/anObject/anAttribute": {
                        "constraint": "Measure",
                        "type": "Number",
                        "timestamp": 1598719840.3,
                        "value": "37.22"
                    },
                    "aNode/anObject/anotherAttribute": {
                        "constraint": "Status",
                        "type": "String",
                        "timestamp": 1598719840.5,
                        "value": "Running"
                    }
                }
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    @Test
    fun emptyTransactionDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeTransaction("""
            {
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing transaction.")
    }

    /*
     * Delayed messages deserialization.
     */

    @Test
    fun validDelayedMessagesDeserialize() {
        val delayedMessages = JSONSerializationFormat().deserializeDelayedMessages("""
            {
                "timestamp": 12345678.55,
                "messages": [
                    {
                        "topic": "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }, {
                        "topic": "@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "attributes": {
                                "aNode/anObject/anAttribute": {
                                    "constraint": "Measure",
                                    "type": "Number",
                                    "timestamp": 1598719840.3,
                                    "value": 37.22
                                },
                                "aNode/anObject/anotherAttribute": {
                                    "constraint": "Status",
                                    "type": "String",
                                    "timestamp": 1598719840.5,
                                    "value": "Running"
                                }
                            }
                        }
                    }, {
                        "topic": "@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "level": "DEBUG",
                            "timestamp": 81637463.39,
                            "message": "Test message",
                            "loggerName": "main logger",
                            "logSource": "endpoint"
                        }
                    }, {
                        "topic": "@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }
                ]
            }
        """.trimIndent().toByteArray())
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
            JSONSerializationFormat().deserializeDelayedMessages("""
            {
                "timestamp": 12345678.55,
                "messages": [
                    {
                        "topic": "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }, {
                        "topic": "@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "attributes": {
                                "aNode/anObject/anAttribute": {
                                    "constraint": "Measure",
                                    "type": "Number",
                                    "timestamp": 1598719840.3,
                                    "value": 37.22
                                },
                                "aNode/anObject/anotherAttribute": {
                                    "constraint": "Status",
                                    "type": "String",
                                    "timestamp": 1598719840.5,
                                    "value": "Running"
                                }
                            }
                        }
                    }, {
                        "topic": "@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "level": "DEBUG",
                            "timestamp": 81637463.39,
                            "message": "Test message",
                            "loggerName": "main logger",
                            "logSource": "endpoint"
                        }
                    }, {
                        "topic": "@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }, {
                        "topic": "@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }
                ]
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun missingTimestampDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages("""
            {
                "messages": [
                    {
                        "topic": "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }, {
                        "topic": "@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "attributes": {
                                "aNode/anObject/anAttribute": {
                                    "constraint": "Measure",
                                    "type": "Number",
                                    "timestamp": 1598719840.3,
                                    "value": 37.22
                                },
                                "aNode/anObject/anotherAttribute": {
                                    "constraint": "Status",
                                    "type": "String",
                                    "timestamp": 1598719840.5,
                                    "value": "Running"
                                }
                            }
                        }
                    }, {
                        "topic": "@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "level": "DEBUG",
                            "timestamp": 81637463.39,
                            "message": "Test message",
                            "loggerName": "main logger",
                            "logSource": "endpoint"
                        }
                    }, {
                        "topic": "@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }
                ]
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun missingMessagesDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages("""
            {
                "timestamp": 12345678.55
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun additionalPropertyDelayedMessagesDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages("""
            {
                "weather": "clear",
                "timestamp": 12345678.55,
                "messages": [
                    {
                        "topic": "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }, {
                        "topic": "@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "attributes": {
                                "aNode/anObject/anAttribute": {
                                    "constraint": "Measure",
                                    "type": "Number",
                                    "timestamp": 1598719840.3,
                                    "value": 37.22
                                },
                                "aNode/anObject/anotherAttribute": {
                                    "constraint": "Status",
                                    "type": "String",
                                    "timestamp": 1598719840.5,
                                    "value": "Running"
                                }
                            }
                        }
                    }, {
                        "topic": "@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66",
                        "data": {
                            "level": "DEBUG",
                            "timestamp": 81637463.39,
                            "message": "Test message",
                            "loggerName": "main logger",
                            "logSource": "endpoint"
                        }
                    }, {
                        "topic": "@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node.object.attribute",
                        "data": {
                            "constraint": "SetPoint",
                            "type": "String",
                            "timestamp": 1598719840.3,
                            "value": "TEST"
                        }
                    }
                ]
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }

    @Test
    fun emptyDelayedMessageDeserialize() {
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeDelayedMessages("""
            {
            }
        """.trimIndent().toByteArray())
        }
        assert(exception.message == "Error deserializing delayed messages.")
    }
}

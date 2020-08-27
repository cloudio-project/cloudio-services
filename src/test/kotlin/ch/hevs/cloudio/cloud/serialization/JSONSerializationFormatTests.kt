package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.AttributeType
import org.junit.Assert.assertThrows
import org.junit.Test

class JSONSerializationFormatTests {
    @Test
    fun staticBooleanAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun staticIntegerAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
        assert(attribute.value == 42)
    }

    @Test
    fun staticNumberAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun staticStringAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun parameterBooleanAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun parameterIntegerAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
        assert(attribute.value == 666)
    }

    @Test
    fun parameterNumberAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun parameterStringAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun setPointBooleanAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun setPointIntegerAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
        assert(attribute.value == 1977)
    }

    @Test
    fun setPointNumberAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun setPointStringAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun statusBooleanAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun statusIntegerAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
        assert(attribute.value == -69)
    }

    @Test
    fun statusNumberAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun statusStringAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun measureBooleanAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun measureIntegerAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
        assert(attribute.value == 80486)
    }

    @Test
    fun measureNumberAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun measureStringAttribute() {
        val attribute = Attribute()
        JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun nullValueAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun invalidConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun nonExistingConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun invalidTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun nonExistingTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun booleanTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun stringTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun negativeTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun nonMatchingTypesAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun missingConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun missingTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun missingTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun missingValueAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
    fun additionalFieldAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            JSONSerializationFormat().deserializeAttribute(attribute, """
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
}

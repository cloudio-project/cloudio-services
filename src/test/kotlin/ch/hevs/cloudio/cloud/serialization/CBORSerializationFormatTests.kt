package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.AttributeType
import org.junit.Assert.assertThrows
import org.junit.Test

/*
 * JSON converted to CBOR using http://cbor.me.
 * Then replaced "#" by "//" and "([0-9,A-F]{2})" by "0x$1, ".
 * Removed last coma.
 */

class CBORSerializationFormatTests {
    @Test
    fun staticBooleanAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun staticIntegerAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
        assert(attribute.value == 42)
    }

    @Test
    fun staticNumberAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun staticStringAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun parameterBooleanAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun parameterIntegerAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
        assert(attribute.value == 666)
    }

    @Test
    fun parameterNumberAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun parameterStringAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun setPointBooleanAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun setPointIntegerAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
        assert(attribute.value == 1977)
    }

    @Test
    fun setPointNumberAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun setPointStringAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun statusBooleanAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun statusIntegerAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
        assert(attribute.value == -69)
    }

    @Test
    fun statusNumberAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun statusStringAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun measureBooleanAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun measureIntegerAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
        assert(attribute.value == 80486)
    }

    @Test
    fun measureNumberAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun measureStringAttribute() {
        val attribute = Attribute()
        CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun nullValueAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun invalidConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun nonExistingConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun invalidTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun nonExistingTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun booleanTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun stringTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun negativeTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun nonMatchingTypesAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun missingConstraintAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun missingTypeAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun missingTimestampAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun missingValueAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
    fun additionalFieldAttribute() {
        val attribute = Attribute()
        val exception = assertThrows(SerializationException::class.java) {
            CBORSerializationFormat().deserializeAttribute(attribute, arrayOf(
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
}

package ch.hevs.cloudio.cloud.serialization.wot

import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.EndpointDataModel


object WotSerializationFormat {

    fun wotNodeFromCloudioNode(endpoint: EndpointDataModel, endpointName: String, nodeName: String, host: String): NodeThingDescription? {

        val mqttHost = host.replace("https", "mqtts").replace("http", "mqtts").replace("8081", "8883")
        val node = endpoint.nodes.get(nodeName)
        if (node != null) {

            val propertiesMap: MutableMap<String, PropertyAffordance> = mutableMapOf()
            val eventMap: MutableMap<String, EventAffordance> = mutableMapOf()


            for (cloudioObject in node.objects) {
                propertiesMap.putAll(buildProperties("$endpointName/$nodeName/${cloudioObject.key}", cloudioObject.value, host, mqttHost))

                /* TODO: Once events are implemented, return those here.
                eventMap.putAll(buildEvents(cloudioObject.key, "$endpointName/$nodeName/${cloudioObject.key}", cloudioObject.value, host, mqttHost))
                 */
            }

            val securityDefinition = mapOf(
                    "https_sc" to SecurityDefinition(scheme = "basic", input = "query") /* TODO: Leaving MQTT out for the moment. ,
                    "mqtts_sc" to SecurityDefinition(scheme = "cert", input = null)*/
            )

            return NodeThingDescription(
                    context = "https://www.w3.org/2019/wot/td/v1",
                    id = "urn:$endpointName:$nodeName",
                    title = nodeName,
                    securityDefinitions = securityDefinition,
                    security = securityDefinition.keys,
                    properties = propertiesMap,
                    events = eventMap
            )
        } else {
            return null
        }
    }

    private fun buildProperties(cloudioObjectTopic: String, cloudioObject: CloudioObject, host: String, mqttHost: String): MutableMap<String, PropertyAffordance> {

        val propertiesMap: MutableMap<String, PropertyAffordance> = mutableMapOf()

        for (innerCloudioObject in cloudioObject.objects) {
            propertiesMap.putAll(buildProperties(cloudioObjectTopic + "/" + innerCloudioObject.key, innerCloudioObject.value, host, mqttHost))
        }

        for (cloudioAttribute in cloudioObject.attributes) {
            val forms: MutableSet<Form> = mutableSetOf()
            when (cloudioAttribute.value.constraint) {
                AttributeConstraint.Invalid -> {
                }
                AttributeConstraint.Static,
                AttributeConstraint.Status,
                AttributeConstraint.Measure -> {
                    forms.add(Form(
                            href = "$host/api/v1/data/" + cloudioObjectTopic + "/" + cloudioAttribute.key,
                            op = "readproperty",
                            subprotocol = null,
                            contentType = "application/json"
                    ))
                }
                AttributeConstraint.Parameter,
                AttributeConstraint.SetPoint -> {
                    forms.add(Form(
                            href = "$host/api/v1/data/" + cloudioObjectTopic + "/" + cloudioAttribute.key,
                            op = "readproperty",
                            subprotocol = null,
                            contentType = "application/json"
                    ))
                    /* TODO: Leaving out MQTT for the moment.
                    forms.add(Form(
                            href = mqttHost + "/@set/" + cloudioObjectTopic + "/" + cloudioAttribute.key,
                            op = "writeproperty",
                            subprotocol = null,
                            contentType = "application/json"
                    ))*/
                    forms.add(Form(
                            href = "$host/api/v1/data/" + cloudioObjectTopic + "/" + cloudioAttribute.key,
                            op = "writeproperty",
                            subprotocol = null,
                            contentType = "application/json"
                    ))
                }

            }

            var wotAttribute = PropertyAffordance(
                    type = "object",
                    properties = mapOf(
                            "constraint" to DataSchema("string", setOf(cloudioAttribute.value.constraint.name)),
                            "type" to DataSchema("string", setOf(cloudioAttribute.value.type.name)),
                            "timestamp" to DataSchema("number", null),
                            "value" to DataSchema(cloudioAttribute.value.type.toString().decapitalize(), null)
                    ),
                    forms = forms,
                    required = setOf("constraint", "type", "timestamp", "value"),
                    enum = null
            )
            propertiesMap[cloudioObjectTopic.replace("/", ".") + "." + cloudioAttribute.key] = wotAttribute
        }

        return propertiesMap
    }

    private fun buildEvents(cloudioObjectName: String, cloudioNodeObjectTopic: String, cloudioObject: CloudioObject, host: String, mqttHost: String): MutableMap<String, EventAffordance> {

        val eventSet: MutableMap<String, EventAffordance> = mutableMapOf()

        for (innerCloudioObject in cloudioObject.objects) {
            eventSet.putAll(buildEvents(cloudioObjectName + innerCloudioObject.key.capitalize(),
                    cloudioNodeObjectTopic + "/" + innerCloudioObject.key.capitalize(),
                    innerCloudioObject.value,
                    host, mqttHost))
        }

        for (cloudioAttribute in cloudioObject.attributes) {
            when (cloudioAttribute.value.constraint) {
                AttributeConstraint.Invalid,
                AttributeConstraint.Static -> {
                }
                AttributeConstraint.Parameter,
                AttributeConstraint.SetPoint -> {
                    eventSet.put(cloudioNodeObjectTopic.replace("/", ".") + "." + cloudioAttribute.key, EventAffordance(
                            data = ObjectSchema(type = "object",
                                    properties = mapOf(
                                            "constraint" to DataSchema("string", setOf(cloudioAttribute.value.constraint.name)),
                                            "type" to DataSchema("string", setOf(cloudioAttribute.value.type.name)),
                                            "timestamp" to DataSchema("number", null),
                                            "value" to DataSchema(cloudioAttribute.value.type.toString().decapitalize(), null)
                                    ),
                                    required = setOf("constraint", "type", "timestamp", "value"),
                                    enum = null
                            ),
                            forms = setOf(Form(
                                    href = "$host/api/v1/notifyAttributeChange/" + cloudioNodeObjectTopic.replace("/", ".") + "." + cloudioAttribute.key + "/15000",
                                    op = "subscribeevent",
                                    subprotocol = "longpoll",
                                    contentType = "application/json"),
                                    Form(
                                            href = mqttHost + "/@set/" + cloudioNodeObjectTopic + "/" + cloudioAttribute.key,
                                            op = "subscribeevent",
                                            subprotocol = null,
                                            contentType = "application/json"))))

                }
                AttributeConstraint.Status,
                AttributeConstraint.Measure -> {
                    eventSet.put(cloudioNodeObjectTopic.replace("/", ".") + "." + cloudioAttribute.key, EventAffordance(
                            data = ObjectSchema(type = "object",
                                    properties = mapOf(
                                            "constraint" to DataSchema("string", setOf(cloudioAttribute.value.constraint.name)),
                                            "type" to DataSchema("string", setOf(cloudioAttribute.value.type.name)),
                                            "timestamp" to DataSchema("number", null),
                                            "value" to DataSchema(cloudioAttribute.value.type.toString().decapitalize(), null)
                                    ),
                                    required = setOf("constraint", "type", "timestamp", "value"),
                                    enum = null
                            ),
                            forms = setOf(Form(
                                    href = "$host/api/v1/notifyAttributeChange/" + cloudioNodeObjectTopic.replace("/", ".") + "." + cloudioAttribute.key + "/15000",
                                    op = "subscribeevent",
                                    subprotocol = "longpoll",
                                    contentType = "application/json"),
                                    Form(
                                            href = mqttHost + "/@update/" + cloudioNodeObjectTopic + "/" + cloudioAttribute.key,
                                            op = "subscribeevent",
                                            subprotocol = null,
                                            contentType = "application/json"))))
                }
            }
        }
        return eventSet
    }
}
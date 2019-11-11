package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.serialization.wot.*


object JsonWotSerializationFormat {

    fun wotNodeFromCloudioNode(endpoint: Endpoint, endpointName: String, nodeName: String, host: String): WotNode?{

        val mqttHost = host.replace("https","mqtts").replace("8081","8883")
        val node = endpoint.nodes.get(nodeName)
        if(node != null){

            val propertiesMap : MutableMap<String, WotObject> = mutableMapOf()
            val eventMap : MutableMap<String, Event> = mutableMapOf()


            for(cloudioObject in node.objects){
                val properties = buildProperties(endpointName+"/"+cloudioObject.key, cloudioObject.value, host, mqttHost)
                propertiesMap[cloudioObject.key] = properties
                eventMap.putAll(buildEvents(cloudioObject.key, endpointName+"/"+cloudioObject.key, cloudioObject.value, host, mqttHost))
            }

            val securityDefinition = mapOf("https_sc" to SecurityDefinition(scheme="basic",input = "query"),
                                        "mqtts_sc" to SecurityDefinition(scheme="cert",input = null))

            return WotNode(
                    context = "https://www.w3.org/2019/wot/td/v1",
                    id = "urn:$endpointName:$nodeName",
                    title = nodeName,
                    securityDefinitions = securityDefinition,
                    security = securityDefinition.keys,
                    properties = propertiesMap,
                    events = eventMap
            )
        }
        else{
            return null
        }
    }

    private fun buildProperties(cloudioObjectTopic: String, cloudioObject: CloudioObject, host: String, mqttHost: String): WotObject{

        val propertiesMap : MutableMap<String, WotObject> = mutableMapOf()

        for(innerCloudioObject in cloudioObject.objects) {
            val properties = buildProperties(cloudioObjectTopic+"/"+innerCloudioObject.key, innerCloudioObject.value, host, mqttHost)
            propertiesMap[innerCloudioObject.key]= properties
        }

        for(cloudioAttribute in cloudioObject.attributes){
            val forms : MutableSet<Form> = mutableSetOf()
            when(cloudioAttribute.value.constraint){
                AttributeConstraint.Invalid ->{}
                AttributeConstraint.Static,
                AttributeConstraint.Status,
                AttributeConstraint.Measure ->{
                    forms.add(Form(
                            href = "$host/api/v1/getAttribute",
                            op = "readproperty",
                            subprotocol=null
                    ))
                }
                AttributeConstraint.Parameter,
                AttributeConstraint.SetPoint->{
                    forms.add(Form(
                            href = "$host/api/v1/getAttribute",
                            op = "readproperty",
                            subprotocol=null
                    ))
                    forms.add(Form(
                            href =mqttHost+"/@set/"+cloudioObjectTopic+"/"+cloudioAttribute.key,
                            op = "writeproperty",
                            subprotocol=null
                    ))
                    forms.add(Form(
                            href = "$host/api/v1/setAttribute",
                            op = "writeproperty",
                            subprotocol=null
                    ))
                }

            }

            var wotAttribute = WotObject(
                    type=cloudioAttribute.value.type.toString(),
                    properties = null,
                    forms = forms
            )
            propertiesMap[cloudioAttribute.key] = wotAttribute
        }

        var wotObject = WotObject(
                type="object",
                properties = propertiesMap,
                forms = null
        )

        return wotObject
    }

    private fun buildEvents(cloudioObjectName: String, cloudioNodeObjectTopic: String, cloudioObject: CloudioObject, host: String, mqttHost: String): MutableMap<String, Event>{

        val eventSet : MutableMap<String, Event> = mutableMapOf()

        for(innerCloudioObject in cloudioObject.objects) {
            eventSet.putAll(buildEvents(cloudioObjectName+innerCloudioObject.key.capitalize(),
                    cloudioNodeObjectTopic+"/"+innerCloudioObject.key.capitalize(),
                    innerCloudioObject.value,
                    host, mqttHost))
        }

        for(cloudioAttribute in cloudioObject.attributes){
            when(cloudioAttribute.value.constraint){
                AttributeConstraint.Invalid,
                AttributeConstraint.Static->{}
                AttributeConstraint.Parameter,
                AttributeConstraint.SetPoint->{
                    eventSet.put("update"+cloudioObjectName.capitalize()+cloudioAttribute.key.capitalize(),Event(
                            data= Data( type = cloudioAttribute.value.type.toString()),
                            forms = setOf(Form(
                                    href = "$host/api/v1/notifyAttributeChange",
                                    op = "longpoll",
                                    subprotocol="observeProperty"),
                                    Form(
                                            href = mqttHost+"/@set/"+cloudioNodeObjectTopic+cloudioAttribute.key,
                                            op = "subscribeEvent",
                                            subprotocol=null))))

                }
                AttributeConstraint.Status,
                AttributeConstraint.Measure->{
                    eventSet.put("update"+cloudioObjectName.capitalize()+cloudioAttribute.key.capitalize(),Event(
                            data= Data( type = cloudioAttribute.value.type.toString()),
                            forms = setOf(Form(
                                    href = "$host/api/v1/notifyAttributeChange",
                                    op = "longpoll",
                                    subprotocol="observeProperty"),
                                    Form(
                                            href = mqttHost+"/@update/"+cloudioNodeObjectTopic+cloudioAttribute.key,
                                            op = "subscribeEvent",
                                            subprotocol=null))))
                }
            }
        }
        return eventSet
    }
}
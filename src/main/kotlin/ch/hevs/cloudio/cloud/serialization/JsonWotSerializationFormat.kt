package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.serialization.wot.*


object JsonWotSerializationFormat {

    val host = "localhost:8080/"

    fun wotNodeFromCloudioNode(endpoint: Endpoint, endpointName: String, nodeName: String): WotNode?{
        val node = endpoint.nodes.get(nodeName)
        if(node != null){

            val propertiesMap : MutableMap<String, WotObject> = mutableMapOf()
            val eventMap : MutableMap<String, Event> = mutableMapOf()


            for(cloudioObject in node.objects){
                val properties = buildProperties(endpointName+"/"+cloudioObject.key, cloudioObject.value)
                propertiesMap[cloudioObject.key] = properties
                eventMap.putAll(buildEvents(cloudioObject.key, endpointName+"/"+cloudioObject.key, cloudioObject.value))
            }

            val securityDefinition = mapOf("http_sc" to SecurityDefinition(scheme="basic",input = "query"),
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

    private fun buildProperties(cloudioObjectTopic: String, cloudioObject: CloudioObject): WotObject{

        val propertiesMap : MutableMap<String, WotObject> = mutableMapOf()

        for(innerCloudioObject in cloudioObject.objects) {
            val properties = buildProperties(cloudioObjectTopic+"/"+innerCloudioObject.key, innerCloudioObject.value)
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
                            href ="http",
                            op = "readproperty",
                            subprotocol=null
                    ))
                }
                AttributeConstraint.Parameter,
                AttributeConstraint.SetPoint->{
                    forms.add(Form(
                            href ="http",
                            op = "readproperty",
                            subprotocol=null
                    ))
                    forms.add(Form(
                            href ="mqtts://"+host+"@set/"+cloudioObjectTopic+"/"+cloudioAttribute.key,
                            op = "writeproperty",
                            subprotocol=null
                    ))
                    forms.add(Form(
                            href ="http",
                            op = "writeproperty",
                            subprotocol=null
                    ))
                }

            }

            var wotAttribute = WotObject(
                    type=cloudioAttribute.value.type.toString(),
                    properties = emptyMap(),
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

    private fun buildEvents(cloudioObjectName: String, cloudioNodeObjectTopic: String, cloudioObject: CloudioObject): MutableMap<String, Event>{

        val eventSet : MutableMap<String, Event> = mutableMapOf()

        for(innerCloudioObject in cloudioObject.objects) {
            eventSet.putAll(buildEvents(cloudioObjectName+innerCloudioObject.key.capitalize(),
                    cloudioNodeObjectTopic+"/"+innerCloudioObject.key.capitalize(),
                    innerCloudioObject.value))
        }

        for(cloudioAttribute in cloudioObject.attributes){
            when(cloudioAttribute.value.constraint){
                AttributeConstraint.Invalid,
                AttributeConstraint.Static->{}
                AttributeConstraint.Parameter,
                AttributeConstraint.Status,
                AttributeConstraint.SetPoint,
                AttributeConstraint.Measure->{
                    eventSet.put("update"+cloudioObjectName.capitalize()+cloudioAttribute.key.capitalize(),Event(
                            data= Data( type = cloudioAttribute.value.type.toString()),
                            forms = setOf(Form(
                                        href ="https",
                                        op = "longpoll",
                                        subprotocol="observeProperty"),
                                    Form(
                                        href ="mqtts://"+host+"@update/"+cloudioNodeObjectTopic+cloudioAttribute.key,
                                        op = "subscribeEvent",
                                        subprotocol=null))))
                }
            }
        }
        return eventSet
    }
}
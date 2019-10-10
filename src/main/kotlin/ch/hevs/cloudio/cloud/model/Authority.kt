package ch.hevs.cloudio.cloud.model

enum class Authority(val value: String) {
    BROKER_ADMINISTRATION("administrator"), //admin login rabbit mq
    BROKER_MONITORING("monitoring"),        //monitoring rabbit mq
    BROKER_POLICYMAKER("policymaker"),      //policymaker rabbit mq
    BROKER_MANAGEMENT("management"),        // management rabbit mq
    HTTP_ACCESS("http_access"),             //access to rest api
    HTTP_ADMIN("http_admin")                //access to admin rest api
}
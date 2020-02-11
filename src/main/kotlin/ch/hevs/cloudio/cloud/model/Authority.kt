package ch.hevs.cloudio.cloud.model

enum class Authority(val value: String) {
    BROKER_ADMINISTRATION("administrator"), // Can log into RabbitMQ management portal as administrator.
    BROKER_MONITORING("monitoring"),        // Can log into RabbitMQ management portal with monitoring role.
    BROKER_POLICYMAKER("policymaker"),      // Can log into RabbitMQ management portal with policymaker role.
    BROKER_MANAGEMENT("management"),        // Can log into RabbitMQ management portal with management role.
    HTTP_ACCESS("http_access"),             // Can access the cloud.iO REST API as normal user.
    HTTP_ADMIN("http_admin")                // Can access the cloud.iO REST API as administrator.
}

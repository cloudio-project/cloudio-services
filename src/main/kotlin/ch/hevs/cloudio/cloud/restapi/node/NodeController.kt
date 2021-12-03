package ch.hevs.cloudio.cloud.restapi.node

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.lang.management.ManagementFactory
import java.net.InetAddress

@Profile("rest-api")
@Api(
    tags = ["Node info"],
    description = "Information about the cloud.iO service node."
)
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("api/v1/node")
class NodeController(
    private val buildProperties: BuildProperties
) {
    @GetMapping("/info")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Returns node info.")
    fun getNodeInfo(@ApiIgnore authentication: Authentication?) = mutableMapOf(
        "version" to buildProperties.version,
        "commit" to buildProperties["shortHash"],
        "code" to "https://github.com/cloudio-project/cloudio-services/commits/${buildProperties["hash"]}",
        "build date" to buildProperties.time
    ).apply {
        if (authentication?.authorities?.contains(SimpleGrantedAuthority(Authority.HTTP_ADMIN.toString())) == true) {
            set("host", InetAddress.getLocalHost().hostName)
            set("address", InetAddress.getLocalHost().hostAddress)
            ManagementFactory.getRuntimeMXBean().also {
                set("jvm", "${it.vmName} ${it.vmVersion}")
                set("uptime", it.uptime)
                set("pid", it.pid)
            }
            ManagementFactory.getMemoryMXBean().also {
                set("memory", mapOf(
                    "heap" to mapOf(
                        "init" to it.heapMemoryUsage.init,
                        "used" to it.heapMemoryUsage.used,
                        "comitted" to it.heapMemoryUsage.committed,
                        "max" to it.heapMemoryUsage.max
                    ),
                    "other" to mapOf(
                        "init" to it.nonHeapMemoryUsage.init,
                        "used" to it.nonHeapMemoryUsage.used,
                        "comitted" to it.nonHeapMemoryUsage.committed
                    )
                ))
            }
            ManagementFactory.getThreadMXBean().also {
                set("threads", it.threadCount)
                set("peak-threads", it.peakThreadCount)
            }
        }
    }
}

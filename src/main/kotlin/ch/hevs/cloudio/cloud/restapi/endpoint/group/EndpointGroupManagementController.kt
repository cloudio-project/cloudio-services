package ch.hevs.cloudio.cloud.restapi.endpoint.group

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@Profile("rest-api")
@Api(tags = ["Endpoint Group Management"], description = "Allows an admin user to manage endpoint groups.")
@RequestMapping("/api/v1/endpoints")
@Authority.HttpAdmin
class EndpointGroupManagementController(
        private var endpointGroupRepository: EndpointGroupRepository,
        private var endpointRepository: EndpointRepository
) {
    @ApiOperation("List all endpoint group names.")
    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups() = endpointGroupRepository.findAll().map { it.groupName }

    @ApiOperation("Create a new endpoint group.")
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun createGroup(@RequestBody body: EndpointGroupEntity) {
        if (endpointGroupRepository.existsByGroupName(body.name)) {
            throw CloudioHttpExceptions.Conflict("Group '${body.name}' exists.")
        }
        endpointGroupRepository.save(EndpointGroup(
                groupName = body.name,
                metaData = body.metaData.toMutableMap()
        ))
    }

    @ApiOperation("Get endpoint group information.")
    @GetMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getGroupByGroupName(@PathVariable groupName: String) = endpointGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        EndpointGroupEntity(
                name = groupName,
                metaData = metaData
        )
    }

    @ApiOperation("Modify endpoint group.")
    @PutMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateGroupByGroupName(@PathVariable groupName: String, @RequestBody body: EndpointGroupEntity) {
        if (groupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match.")
        }
        endpointGroupRepository.findByGroupName(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.run {
            metaData = body.metaData.toMutableMap()
            endpointGroupRepository.save(this)
        }
    }

    @ApiOperation("Deletes endpoint group.")
    @DeleteMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteGroupByGroupName(@PathVariable groupName: String) = endpointGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        endpointRepository.findByGroupMembershipsContains(this).forEach {
            it.groupMemberships.remove(this)
            endpointRepository.save(it)
        }
        endpointGroupRepository.delete(this)
    }
}

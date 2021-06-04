package demo.controllers

import demo.models.people.Organization
import demo.services.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(@Autowired private val organizationService: OrganizationService) {
    @GetMapping
    fun getOrganizations(): ResponseEntity<MutableIterable<Organization>> {
        return ResponseEntity(organizationService.getOrganizations(), HttpStatus.OK)
    }

    @GetMapping("/{organizationId}")
    fun getOrganization(@PathVariable organizationId: OrganizationID): ResponseEntity<Organization> {
        val organization: Organization = organizationService.getOrganization(organizationId.toId())

        return  ResponseEntity(organization, HttpStatus.OK)
    }

    @PostMapping("/addorganization")
    fun addOrganization(@RequestBody organization: OrganizationRequest): ResponseEntity<Organization> {
        val saved: Organization = organizationService.addOrganization(organization.toOffer())

        val httpHeaders = HttpHeaders()
        httpHeaders.add(HttpHeaders.ACCEPT, "/api/organizations/" + saved.id)
        return ResponseEntity(httpHeaders, HttpStatus.CREATED)
    }

    @PutMapping("/{organizationId}")
    fun updateOrganization(@PathVariable("organizationId") organizationId: OrganizationID, @RequestBody organization: OrganizationRequest): ResponseEntity<Organization> {
        organizationService.updateOrganization(organizationId.toId(), organization.toOffer())
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(@PathVariable("organizationId") organizationId: OrganizationID): ResponseEntity<Organization> {
        organizationService.deleteOrganization(organizationId.toId())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(): ResponseEntity<Any> {
        return ResponseEntity("No resource with such id", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(): ResponseEntity<Any> {
        return ResponseEntity("Unknown error occurred", HttpStatus.BAD_REQUEST)
    }


    fun OrganizationRequest.toOrganization() = Organization(null, name, verified)

    fun OrganizationID.toId(): UUID = UUID.fromString(id)
}

data class OrganizationRequest(val name: String, val verified: Boolean?)
data class OrganizationID(val id: String)

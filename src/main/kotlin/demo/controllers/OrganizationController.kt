package demo.controllers

import demo.models.people.Organization
import demo.services.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(@Autowired private val organizationService: OrganizationService) {
    @GetMapping
    fun getOrganizations(): ResponseEntity<MutableIterable<Organization>> {
        return ResponseEntity(organizationService.getOrganizations(), HttpStatus.OK)
    }

    @GetMapping("/{organizationId}")
    fun getOrganization(@PathVariable organizationId: String): ResponseEntity<Organization> {
        val organization: Optional<Organization> = organizationService.getOrganization(organizationId)
        return if (organization.isPresent)
            ResponseEntity(organizationService.getOrganization(organizationId).get(), HttpStatus.OK)
        else
            ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @PostMapping("/addorganization")
    fun addOrganization(@RequestBody organization: Organization): ResponseEntity<Organization> {
        val saved: Organization = organizationService.addOrganization(organization)

        val httpHeaders = HttpHeaders()
        httpHeaders.add(HttpHeaders.ACCEPT, "/api/organization/" + saved.id)
        return ResponseEntity(saved, httpHeaders, HttpStatus.CREATED)
    }

    @PutMapping("update/{organizationId}")
    fun updateOrganization(@PathVariable("organizationId") organizationId: String, @RequestBody organization: Organization): ResponseEntity<Organization> {
        organizationService.updateOrganization(organizationId, organization)
        return ResponseEntity(organizationService.getOrganization(organizationId).get(), HttpStatus.OK)
    }

    @DeleteMapping("/{organizationId}")
    fun deleteOrganization(@PathVariable("organizationId") organizationId: String): ResponseEntity<Organization> {
        organizationService.deleteOrganization(organizationId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
package demo.controllers

import demo.models.people.HrPartner
import demo.services.HrPartnerService
import demo.services.OrganizationService
import demo.services.UserService
import org.apache.coyote.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/hrpartners")
class HrPartnerController(@Autowired private val hrPartnerService: HrPartnerService,
                          @Autowired private val organizationService: OrganizationService,
                          @Autowired private val userService: UserService) {
    @GetMapping
    fun getHrPartners(): ResponseEntity<MutableIterable<HrPartner>> {
        return ResponseEntity(hrPartnerService.getHrPartners(), HttpStatus.OK)
    }

    @GetMapping("/{hrPartnerId}")
    fun getHrPartner(@PathVariable("hrPartnerId") hrPartnerId: Int): ResponseEntity<HrPartner> {
        val partner: HrPartner = hrPartnerService.getHrPartner(hrPartnerId)

        return ResponseEntity(partner, HttpStatus.OK)
    }

    @PostMapping("/addHrPartner")
    fun addHrPartner(@RequestBody hrPartner: HrPartnerRequest): ResponseEntity<HrPartner> {
        val saved: HrPartner = hrPartnerService.addHrPartner(hrPartner.toHrPartner())

        return ResponseEntity.created(URI("/api/hrpartners/" + saved.id)).build()
    }

    fun HrPartnerRequest.toHrPartner() = HrPartner(null,
        organizationService.getOrganization(UUID.fromString(organizationId)),
        userService.getUser(userId))
}

data class HrPartnerRequest(val organizationId: String, val userId: Int)
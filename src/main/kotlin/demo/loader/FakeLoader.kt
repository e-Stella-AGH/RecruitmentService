package demo.loader

import demo.models.offers.Offer
import demo.models.offers.RecruitmentProcess
import demo.models.offers.RecruitmentStage
import demo.models.offers.StageType
import demo.models.people.HrPartner
import demo.models.people.JobSeeker
import demo.models.people.Organization
import demo.models.people.User
import demo.repositories.HrPartnerRepository
import demo.repositories.JobSeekerRepository
import demo.repositories.OfferRepository
import demo.repositories.OrganizationRepository
import demo.repositories.RecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.sql.Clob
import java.sql.Date
import java.time.LocalDate
import java.util.*
import javax.sql.rowset.serial.SerialClob
import javax.transaction.Transactional

@Component
@Transactional
data class FakeLoader(
        @Autowired val organizationRepository: OrganizationRepository,
        @Autowired val hrPartnerRepository: HrPartnerRepository,
        @Autowired val jobSeekerRepository: JobSeekerRepository,
        @Autowired val offerRepository: OfferRepository,
        @Autowired val recruitmentProcessRepository: RecruitmentProcessRepository
) {
    @EventListener
    @Order(1)
    fun appReady(event: ApplicationReadyEvent) {
        val org = organizationRepository.save(Organization(
                id = null,
                name = "Organiz",
                verified = true
        ))
        val jobSeeker = jobSeekerRepository.save(JobSeeker(
                id = null,
                user = User(
                        id = null,
                        firstName = "First",
                        lastName = "Name",
                        mail = "mail",
                        password = "a"
                ),
                files = Collections.emptySet()
        ))
        val hrPartner = hrPartnerRepository.save(HrPartner(
                id = null,
                organization = org,
                user = User(
                        id = null,
                        firstName = "First",
                        lastName = "Name",
                        mail = "mail",
                        password = "a"
                )
        ))
        val offer = offerRepository.save(Offer(
                null,
                "Offer",
                SerialClob("desc".toCharArray()),
                "pos",
                1,
                1,
                "Loc",
                hrPartner,
                Collections.emptySet(),
                null
        ))
        recruitmentProcessRepository.save(RecruitmentProcess(
            null,
            Date.valueOf(LocalDate.now()),
            Date.valueOf(LocalDate.now()),
            offer,
            List(1) { RecruitmentStage(null, StageType.APPLIED) },
            Collections.emptySet(),
            Collections.emptySet()
        ))
    }
}
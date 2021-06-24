package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.process.infrastructure.HibernateDesiredSkillRepository
import org.malachite.estella.process.infrastructure.HibernateRecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
@Transactional
data class FakeLoader(
    @Autowired val organizationRepository: HibernateOrganizationRepository,
    @Autowired val hrPartnerRepository: HibernateHrPartnerRepository,
    @Autowired val jobSeekerRepository: HibernateJobSeekerRepository,
    @Autowired val offerRepository: HibernateOfferRepository,
    @Autowired val recruitmentProcessRepository: HibernateRecruitmentProcessRepository,
    @Autowired val desiredSkillRepository: HibernateDesiredSkillRepository
) {
    @EventListener
    @Order(1)
    fun appReady(event: ApplicationReadyEvent) {
        val companies = FakeOrganizations.companies.map { organizationRepository.save(it) }
        val jobSeekers = FakeUsers.users
            .filterIndexed { index, _ -> index % 2 != 0 }
            .map {
                jobSeekerRepository.save(
                    JobSeeker(
                        id = null,
                        user = it,
                        files = Collections.emptySet()
                    )
                )
            }
        val hrPartners = FakeUsers.users
            .filterIndexed { index, _ -> index % 2 == 0 }
            .mapIndexed { index, user ->
                hrPartnerRepository.save(
                    HrPartner(
                        id = null,
                        user = user,
                        organization = companies[index % companies.size]
                    )
                )
            }
        val desiredSkills = FakeDesiredSkills.desiredSkills.map { desiredSkillRepository.save(it) }
        val offers = FakeOffers.getOffers(hrPartners,desiredSkills).map { offerRepository.save(it) }

        val recruitmentProcesses = FakeRecruitmentProcess.getProcesses(offers).map {
            recruitmentProcessRepository.save(it)
        }
    }
}
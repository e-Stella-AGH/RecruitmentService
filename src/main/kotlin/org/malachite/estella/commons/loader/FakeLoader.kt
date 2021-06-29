package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.process.infrastructure.HibernateDesiredSkillRepository
import org.malachite.estella.process.infrastructure.HibernateRecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.transaction.Transactional


@Bean
fun appReady(
    @Autowired organizationRepository: HibernateOrganizationRepository,
    @Autowired hrPartnerRepository: HibernateHrPartnerRepository,
    @Autowired jobSeekerRepository: HibernateJobSeekerRepository,
    @Autowired offerRepository: HibernateOfferRepository,
    @Autowired recruitmentProcessRepository: HibernateRecruitmentProcessRepository,
    @Autowired desiredSkillRepository: HibernateDesiredSkillRepository
): CommandLineRunner {
    return CommandLineRunner {
        val companies = FakeOrganizations.companies.map { organizationRepository.save(it) }

        jobSeekerRepository.saveAll(FakeLoader.getFakeJobSeekers())

        val hrPartners = FakeLoader.getHrPartners(companies)
        hrPartnerRepository.saveAll(hrPartners)
        val desiredSkills = FakeDesiredSkills.desiredSkills.map { desiredSkillRepository.save(it) }
        val offers = FakeOffers.getOffers(hrPartners, desiredSkills).map { offerRepository.save(it) }

        FakeRecruitmentProcess.getProcesses(offers).map {
            recruitmentProcessRepository.save(it)
        }
    }
}
object FakeLoader {
    fun getHrPartners(companies: List<Organization>) =
        FakeUsers.users
            .filterIndexed { index, _ -> index % 2 == 0 }
            .mapIndexed { index, user ->
                HrPartner(
                    id = null,
                    user = user,
                    organization = companies[index % companies.size]
                )
            }

    fun getFakeJobSeekers() =
        FakeUsers.users
            .filterIndexed { index, _ -> index % 2 != 0 }
            .map {
                JobSeeker(
                    id = null,
                    user = it,
                    files = Collections.emptySet()
                )
            }
}
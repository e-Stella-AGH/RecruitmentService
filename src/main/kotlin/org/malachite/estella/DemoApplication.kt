package org.malachite.estella

import com.beust.klaxon.Klaxon
import org.malachite.estella.commons.loader.*
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.people.infrastrucutre.HibernateUserRepository
import org.malachite.estella.process.infrastructure.HibernateDesiredSkillRepository
import org.malachite.estella.process.infrastructure.HibernateRecruitmentProcessRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class DemoApplication {
    @Bean
    fun appReady(
        @Autowired organizationRepository: HibernateOrganizationRepository,
        @Autowired hrPartnerRepository: HibernateHrPartnerRepository,
        @Autowired jobSeekerRepository: HibernateJobSeekerRepository,
        @Autowired userRepository: HibernateUserRepository,
        @Autowired offerRepository: HibernateOfferRepository,
        @Autowired recruitmentProcessRepository: HibernateRecruitmentProcessRepository,
        @Autowired desiredSkillRepository: HibernateDesiredSkillRepository,
        @Value("\${should_fake_load}") shouldFakeLoad: Boolean
    ): CommandLineRunner {
        return CommandLineRunner {
            if (offerRepository.findAll().count() == 0 && shouldFakeLoad) {
                loadData(
                    organizationRepository,
                    hrPartnerRepository,
                    jobSeekerRepository,
                    offerRepository,
                    recruitmentProcessRepository,
                    desiredSkillRepository
                )
            }
        }
    }
}

fun loadData(
    organizationRepository: HibernateOrganizationRepository,
    hrPartnerRepository: HibernateHrPartnerRepository,
    jobSeekerRepository: HibernateJobSeekerRepository,
    offerRepository: HibernateOfferRepository,
    recruitmentProcessRepository: HibernateRecruitmentProcessRepository,
    desiredSkillRepository: HibernateDesiredSkillRepository
) {
    val organizationUsers = FakeUsers.organizationUsers
        .map { it.copy(id = null) }
        .also { it.map { it.password = "a" } }
    val companies = FakeOrganizations.getCompanies(organizationUsers).map { organizationRepository.save(it) }

    FakeLoader.getFakeJobSeekers()
        .map { it.copy(id = null, user = it.user.copy(id = null)) }
        .also { it.map { it.user.password = "a" } }
        .let { jobSeekerRepository.saveAll(it) }

    val hrPartners = FakeLoader.getHrPartners(companies)
        .map { it.copy(id = null, user = it.user.copy(id = null)) }
        .also { it.map { it.user.password = "a" } }
    hrPartnerRepository.saveAll(hrPartners)
    val desiredSkills = FakeDesiredSkills.desiredSkills.map { desiredSkillRepository.save(it) }
    val offers = FakeOffers.getOffers(hrPartners, desiredSkills).map { offerRepository.save(it) }

    FakeRecruitmentProcess.getProcesses(offers).map {
        recruitmentProcessRepository.save(it)
    }
}

fun main(args: Array<String>) {
    val env: MutableMap<String, String> = System.getenv()
    if (env.containsKey("DATABASE_URL"))
        prepareSpringProperties(env)
    else
        prepareSpringProperties()
    runApplication<DemoApplication>(*args)
}

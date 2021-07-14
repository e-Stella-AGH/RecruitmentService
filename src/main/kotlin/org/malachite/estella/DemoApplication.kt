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
        @Autowired desiredSkillRepository: HibernateDesiredSkillRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            if (offerRepository.findAll().count() == 0) {
                val companies = FakeOrganizations.getCompanies(FakeUsers.organizationUsers).map { organizationRepository.save(it) }

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

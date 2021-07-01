package org.malachite.estella

import com.beust.klaxon.Klaxon
import org.malachite.estella.commons.loader.*
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
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
        @Autowired offerRepository: HibernateOfferRepository,
        @Autowired recruitmentProcessRepository: HibernateRecruitmentProcessRepository,
        @Autowired desiredSkillRepository: HibernateDesiredSkillRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            if (offerRepository.findAll().count() == 0) {
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
    }
}

const val applicationPath = "src/main/resources/application.properties"
const val configPath = "config.json"

fun getConfigurationData(): MutableMap<String, String> {
    val file = File(configPath)
    if (!file.exists())
        return HashMap()
    return Klaxon().parse<MutableMap<String, String>>(File(configPath)) ?: HashMap<String, String>()
}

fun getApplicationPropertiesForSql(env: MutableMap<String, String>): String {
    return """
			spring.datasource.url=${env["DATABASE_URL"]}
            spring.jpa.generate-ddl=true
            spring.jpa.hibernate.ddl-auto=create
			spring.jpa.show-sql=true
			spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
			spring.datasource.driver-class-name=org.postgresql.Driver
		""".trimIndent()
}

fun getApplicationPropertiesForH2(): String {
    return """
        spring.datasource.url=jdbc:h2:file:./myDB
        spring.datasource.username=admin
        spring.datasource.password=admin
        spring.jpa.generate-ddl=true
        spring.jpa.hibernate.ddl-auto=create
        spring.jpa.show-sql=true
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.driverClassName=org.h2.Driver
        spring.h2.console.enabled=true
        springdoc.swagger-ui.path=/docs
    """.trimIndent()
}

fun prepareSpringProperties(env: MutableMap<String, String> = getConfigurationData()) {
    var properties = if (env.isNotEmpty()) getApplicationPropertiesForSql(env) else getApplicationPropertiesForH2()
    properties += "\n" + getOtherApplicationProperties()
    File(applicationPath).printWriter().use { out ->
        out.println(properties)
    }
}

fun getOtherApplicationProperties(): String = """
    mail_service_url=https://email-service-estella.herokuapp.com/
""".trimIndent()

fun main(args: Array<String>) {
    val env: MutableMap<String, String> = System.getenv()
    if (env.containsKey("DATABASE_URL"))
        prepareSpringProperties(env)
    else
        prepareSpringProperties()
    runApplication<DemoApplication>(*args)
}

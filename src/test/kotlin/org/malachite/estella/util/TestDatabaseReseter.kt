package org.malachite.estella.util

import org.malachite.estella.loadData
import org.malachite.estella.offer.infrastructure.HibernateOfferRepository
import org.malachite.estella.organization.infrastructure.HibernateOrganizationRepository
import org.malachite.estella.people.infrastrucutre.HibernateHrPartnerRepository
import org.malachite.estella.people.infrastrucutre.HibernateJobSeekerRepository
import org.malachite.estella.process.infrastructure.HibernateDesiredSkillRepository
import org.malachite.estella.process.infrastructure.HibernateRecruitmentProcessRepository
import org.malachite.estella.task.infrastructure.HibernateTaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(mergeMode =
TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
    listeners = [TestDatabaseReseter::class]
)
annotation class DatabaseReset


@Component
class TestDatabaseReseter: TestExecutionListener {

    private val applicationPath = "src/main/resources/application.properties"

    @Autowired
    private lateinit var databaseClearer: DatabaseClearer
    @Autowired
    private lateinit var organizationRepository: HibernateOrganizationRepository
    @Autowired
    private lateinit var hrPartnerRepository: HibernateHrPartnerRepository
    @Autowired
    private lateinit var jobSeekerRepository: HibernateJobSeekerRepository
    @Autowired
    private lateinit var offerRepository: HibernateOfferRepository
    @Autowired
    private lateinit var recruitmentProcessRepository: HibernateRecruitmentProcessRepository
    @Autowired
    private lateinit var desiredSkillRepository: HibernateDesiredSkillRepository
    @Autowired
    private lateinit var tasksRepository: HibernateTaskRepository

    override fun afterTestClass(testContext: TestContext) {
        testContext.applicationContext
            .autowireCapableBeanFactory
            .autowireBean(this)

        databaseClearer.clearDatabase()
    }

    override fun beforeTestClass(testContext: TestContext) {
        testContext.applicationContext
            .autowireCapableBeanFactory
            .autowireBean(this)

        loadData(
            organizationRepository,
            hrPartnerRepository,
            jobSeekerRepository,
            offerRepository,
            recruitmentProcessRepository,
            desiredSkillRepository,
            tasksRepository
        )
    }

}
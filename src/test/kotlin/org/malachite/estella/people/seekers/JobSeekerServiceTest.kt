package org.malachite.estella.people.seekers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.people.JobSeeker
import org.malachite.estella.people.users.DummyUserRepository
import org.malachite.estella.services.JobSeekerFileService
import org.malachite.estella.services.JobSeekerService
import org.malachite.estella.services.MailService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.util.dev.`null`.mailServiceResponse
import org.malachite.estella.util.jobSeekers
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class JobSeekerServiceTest {

    private val jobSeekerRepository = DummyJobSeekerRepository()
    private val userRepository = DummyUserRepository()
    private val mailServiceMock = mockk<MailService>()
    private val securityServiceMock = mockk<SecurityService>()
    private val jobSeekerFileServiceMock = mockk<JobSeekerFileService>()
    private val service = JobSeekerService(jobSeekerRepository, mailServiceMock,jobSeekerFileServiceMock, securityServiceMock)

    @BeforeEach
    fun setup() {
        every { mailServiceMock.sendRegisterMail(any()) } returns mailServiceResponse
    }

    @AfterEach
    fun cleanup() {
        jobSeekerRepository.clear()
        userRepository.clear()
    }

    @Test
    fun `should be able to register jobSeekers and send mails to them`() {
        testUsers.forEach {
            service.registerJobSeeker(it)
        }
        expectThat(service.getJobSeekers()).isEqualTo(testUsers as MutableIterable<JobSeeker>)
        verify(exactly = testUsers.size) { mailServiceMock.sendRegisterMail(any()) }
    }

    private val testUsers = jobSeekers

}
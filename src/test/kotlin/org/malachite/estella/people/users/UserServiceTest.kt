package org.malachite.estella.people.users

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.organization.domain.OrganizationRepository
import org.malachite.estella.people.api.UserRequest
import org.malachite.estella.people.domain.HrPartnerRepository
import org.malachite.estella.people.domain.JobSeekerRepository
import org.malachite.estella.people.domain.UserNotFoundException
import org.malachite.estella.security.Authority
import org.malachite.estella.security.UserContextDetails
import org.malachite.estella.services.SecurityService
import org.malachite.estella.services.UserService
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.users
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class UserServiceTest {

    private val userRepository = DummyUserRepository()
    private val hrRepository = mockk<HrPartnerRepository>()
    private val jobSeekerRepository = mockk<JobSeekerRepository>()
    private val organizationRepository = mockk<OrganizationRepository>()
    private val securityMock = mockk<SecurityService>()
    private val userService = UserService(securityMock, userRepository, hrRepository, jobSeekerRepository, organizationRepository)

    companion object {
        @BeforeAll
        @JvmStatic
        fun setupTest() {
            EmailServiceStub.stubForSendEmail()
        }
    }

    @BeforeEach
    fun setup() {
        testUsers.forEach { userService.addUser(it) }
        every { securityMock.isCorrectApiKey("abc") } returns false
    }

    @AfterEach
    fun cleanup() {
        userRepository.clear()
    }

    @Test
    fun `should be able to add users`() {
        expectThat(userService.getUsers().count()).isEqualTo(testUsers.size)
    }

    @Test
    fun `should be able to get user by id`() {
        expectThat(userService.getUser(0)).isEqualTo(testUsers[0])
    }

    @Test
    fun `should throw UserNotFoundException when there's no such user`() {
        expectThrows<UserNotFoundException> {
            userService.getUser(10000)
        }
    }

    @Test
    fun `should be able to find user by mail`() {
        expectThat(userService.getUserByEmail(testUsers[0].mail)).isEqualTo(testUsers[0])
    }

    @Test
    fun `should be able to update user`() {
        val user = users[0]
        every { securityMock.getUserFromContext() } returns user
        every { securityMock.getUserDetailsFromContext() } returns UserContextDetails(
                user,
                "abc",
                setOf(Authority.hr),
                true
        )

        every { securityMock.isCorrectApiKey(any()) } returns false
        userService.updateUser(UserRequest( "new First Name", "new Last Name", users[0].mail, ""))
        userService.getUser(users[0].id!!).let {
            expectThat(it.firstName).isEqualTo("new First Name")
            expectThat(it.lastName).isEqualTo("new Last Name")
        }
    }

    @Test
    fun `should throw unauth, when jwt is invalid`() {
        every { securityMock.getUserFromContext() } returns null
        expectThrows<UnauthenticatedException> {
            userService.updateUser(UserRequest( "newFirstName", "", users[0].mail, ""))
        }
    }

    private val testUsers = users

}
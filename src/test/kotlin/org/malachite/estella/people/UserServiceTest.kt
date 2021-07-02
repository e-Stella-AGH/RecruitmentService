package org.malachite.estella.people

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.people.domain.UserNotFoundException
import org.malachite.estella.services.MailService
import org.malachite.estella.services.UserService
import org.malachite.estella.util.EmailServiceStub
import org.malachite.estella.util.users
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.doesNotContain
import strikt.assertions.isEqualTo

class UserServiceTest {

    private val repository = DummyUserRepository()
    private val userService = UserService(repository, MailService("/email"))


    @BeforeEach
    fun setup(){
        testUsers.forEach { userService.addUser(it) }
        EmailServiceStub.stubForSendEmail()
    }

    @AfterEach
    fun cleanup(){
        repository.clear()
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
    fun `should be able to delete user by id`() {
        userService.deleteUser(0)
        expectThat(userService.getUsers().count()).isEqualTo(testUsers.size - 1)
        expectThat(userService.getUsers()).doesNotContain(testUsers[0])
    }

    private val testUsers = users

}
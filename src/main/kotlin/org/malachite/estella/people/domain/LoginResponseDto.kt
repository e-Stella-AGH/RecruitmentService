package org.malachite.estella.people.domain

sealed class LoginResponse()

data class LoginResponseDto(val text: String): LoginResponse()
data class InvalidLoginResponseDto(val errorMessage: String): LoginResponse()

class UserAlreadyExistsException : Exception()
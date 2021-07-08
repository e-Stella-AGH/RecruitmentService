package org.malachite.estella.commons

open class OneStringValueMessage

open class Message(val message: String): OneStringValueMessage()

object SuccessMessage: Message("Success")
data class TokenMessage(val token: String): OneStringValueMessage()
package org.malachite.estella.commons

open class OneStringValueMessage

open class Message(val message: String): OneStringValueMessage()

object SuccessMessage: Message("Success")
object UnauthenticatedMessage: Message("Unauthenticated")
object NoResourceMessage: Message("No resource with such id")

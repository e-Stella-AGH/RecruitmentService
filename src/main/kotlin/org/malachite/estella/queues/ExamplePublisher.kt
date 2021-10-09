package org.malachite.estella.queues

class ExamplePublisher() : Queue() {

    init {
        channel.queueDeclare("hello", false, false, false, null)
        val message = "Hello World!"
        channel.basicPublish("", "hello", null, message.toByteArray(charset("UTF-8")))
        channel.close()
        connection.close()
    }
}
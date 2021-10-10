package org.malachite.estella.queues

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate

class ExamplePublisherv2(
    private val rabbitTemplate: RabbitTemplate
) {

    fun publish() {
        try {
            rabbitTemplate.send("example_queue", Message("hello".toByteArray(), MessageProperties()))
        } catch (e: Exception) {
            println("Couldn't send example message to consumer")
        }
    }

}
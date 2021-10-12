package org.malachite.estella.queue

import org.junit.jupiter.api.Test
import org.malachite.estella.BaseIntegration
import org.malachite.estella.queues.ExamplePublisherv2
import org.malachite.estella.queues.RabbitMqConsumers
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.lang.Thread.sleep

class ExamplePublisherTest: BaseIntegration() {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var consumers: RabbitMqConsumers

    @Test
    fun `example rabbit with spring magic`() {
        val publisher = ExamplePublisherv2(rabbitTemplate)
        repeat(3) {
            publisher.publish()
        }
        sleep(1000)
        expectThat(consumers.expected).isEqualTo(3)
    }

}
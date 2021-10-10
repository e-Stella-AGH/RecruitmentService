package org.malachite.estella.queue

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.queues.ExampleConsumer
import org.malachite.estella.queues.ExamplePublisher
import org.malachite.estella.queues.ExamplePublisherv2
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.lang.Thread.sleep

class QueueExampleIntegration {

    @Test
    @Order(1)
    fun `Example consumer should receive 1 message`() {
        val consumer = ExampleConsumer()
        val publisher = ExamplePublisher()
        publisher.sendMessage()
        sleep(1000)
        expectThat(consumer.received).isEqualTo(1)
        publisher.sendMessage()
        sleep(1000)
        expectThat(consumer.received).isEqualTo(2)
        publisher.close()
        consumer.close()
    }

}
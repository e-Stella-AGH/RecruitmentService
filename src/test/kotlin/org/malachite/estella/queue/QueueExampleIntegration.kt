package org.malachite.estella.queue

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.queues.ExampleConsumer
import org.malachite.estella.queues.ExamplePublisher
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
        sleep(1000)
        assert(consumer.received==1)
        consumer.stopConsumer()
    }

}
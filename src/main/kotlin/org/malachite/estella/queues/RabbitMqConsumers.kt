package org.malachite.estella.queues

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header

class RabbitMqConsumers {

    var expected = 0

    @RabbitListener(
        queues = ["example_queue"],
        ackMode = "MANUAL",
        containerFactory = "containerFactory"
    )
    fun exampleListener(message: String, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) {
        println("Message from rabbit: $message")
        expected++
        channel.basicAck(tag, false)
    }

    //add other consumers

}
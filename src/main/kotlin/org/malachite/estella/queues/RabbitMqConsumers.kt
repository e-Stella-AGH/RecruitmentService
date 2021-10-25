package org.malachite.estella.queues

import com.rabbitmq.client.Channel
import org.malachite.estella.queues.utils.MsgDeserializer
import org.malachite.estella.services.TaskService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header

class RabbitMqConsumers(
        private val taskService: TaskService,
        private val msgDeserializer: MsgDeserializer) {


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

    @RabbitListener(
            queues = ["task_result"],
            ackMode = "MANUAL",
            containerFactory = "containerFactory"
    )
    fun taskResultListener(message: String, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) =
            msgDeserializer.toTaskResultRabbitDTO(message)
                    ?.let {
                        taskService.addResult(it)
                        channel.basicAck(tag, false)
                    }
                    ?.also { println("received Message with task results: $message") }
                    ?: channel.basicNack(tag, false, false)


    //add other consumers

}
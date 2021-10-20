package org.malachite.estella.queues

import com.rabbitmq.client.Channel
import org.malachite.estella.queues.utils.MsgDeserializer
import org.malachite.estella.services.TaskService
import org.malachite.estella.services.TaskStageService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header

class RabbitMqConsumers() {

    @Autowired private lateinit var taskService: TaskService
    @Autowired private lateinit var taskStageService: TaskStageService


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
            MsgDeserializer(taskService, taskStageService).toTaskResult(message)
                    ?.let {
                taskStageService.addResult(it)
                channel.basicAck(tag, false)
            }
                    ?:let {
                channel.basicNack(tag, false, false)
            }

    //add other consumers

}
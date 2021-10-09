package org.malachite.estella.queues

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

class ExampleConsumer() : Queue() {

    var received = 0

    init {
        channel.queueDeclare("hello", false, false, false, null)
        println(" [*] Waiting for messages. To exit press CTRL+C")
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                val message = String(body, charset("UTF-8"))
                received+=1
                println(" [x] Received '$message'")
            }
        }
        channel.basicConsume("hello", true, consumer)
    }

    fun stopConsumer(){
        channel.queueDelete("hello")
    }

}
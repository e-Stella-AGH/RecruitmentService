package org.malachite.estella.queues

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class RabbitConsumersConfiguration {

    @Bean
    fun rabbitConnectionFactory(
        @Value("\${cloud_amqp_url}") cloudAmqpUrl: String
    ): CachingConnectionFactory = CachingConnectionFactory(URI.create(cloudAmqpUrl))

    @Bean
    fun rabbitTemplate(
        connectionFactory: CachingConnectionFactory
    ): RabbitTemplate = RabbitTemplate(connectionFactory)

    @Bean
    fun containerFactory(
        connectionFactory: CachingConnectionFactory
    ): SimpleRabbitListenerContainerFactory = SimpleRabbitListenerContainerFactory().also {
        it.setConnectionFactory(connectionFactory)
    }

    @Bean
    fun rabbitMqConsumers(
        rabbitTemplate: RabbitTemplate
    ): RabbitMqConsumers {
        RabbitAdmin(rabbitTemplate).apply {
            //declare queues, exchanges, bindings, etc
            this.declareQueue(Queue("example_queue"))
            this.declareQueue(Queue("task_result"))
        }
        return RabbitMqConsumers()
    }
}
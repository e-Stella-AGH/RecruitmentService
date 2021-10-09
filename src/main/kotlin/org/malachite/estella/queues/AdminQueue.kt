package org.malachite.estella.queues

import com.beust.klaxon.Klaxon
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import java.io.File
import java.net.URI

object AdminQueue {

    private val env = System.getenv()

    private val cloudamqpKey = "CLOUDAMQP_URL"
    const val configPath = "config.json"

    private fun getCloudAmqpFromConfig(): MutableMap<String, String> {
        val file = File(configPath)
        if (!file.exists()) throw NoSuchFileException(file)
        return Klaxon().parse<MutableMap<String, String>>(file)!!
    }

    fun generateConnectionFactory(): ConnectionFactory {
        val resultEnv: Map<String, String> = if (env.containsKey(cloudamqpKey)) env else getCloudAmqpFromConfig()
        if (!resultEnv.containsKey(cloudamqpKey) || resultEnv[cloudamqpKey] == null) throw Exception("Env doesn't have key")
        val factory = CachingConnectionFactory(URI.create(resultEnv[cloudamqpKey]))
        factory.setRequestedHeartBeat(30)
        factory.setConnectionTimeout(30_000)
        return factory
    }


    fun getRabbitAdmin() = RabbitAdmin(this.generateConnectionFactory())


}
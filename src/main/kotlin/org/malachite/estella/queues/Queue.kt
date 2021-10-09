package org.malachite.estella.queues


abstract class Queue {
    val factory = AdminQueue.generateConnectionFactory()
    val connection = factory.createConnection()
    val channel = connection.createChannel(false)
}
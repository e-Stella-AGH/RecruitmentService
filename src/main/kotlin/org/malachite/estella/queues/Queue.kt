package org.malachite.estella.queues


abstract class Queue {
    private val factory = AdminQueue.factory
    val connection = factory.createConnection()
    val channel = connection.createChannel(false)
}
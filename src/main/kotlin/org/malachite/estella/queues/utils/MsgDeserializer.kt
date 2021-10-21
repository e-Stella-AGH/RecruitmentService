package org.malachite.estella.queues.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.util.*

@Component
class MsgDeserializer {

        fun toTaskResultRabbitDTO(msg: String): TaskResultRabbitDTO? {
            return try {
                Base64.getDecoder().decode(msg).decodeToString().let { Json.decodeFromString<TaskResultRabbitDTO>(it) }
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("DBG: Bad request. Couldn't parse msg to TaskResult: $msg")
                null
            }
        }


}

@Serializable
data class TaskResultRabbitDTO(
        val results: String,
        val code: String,
        val startTime: String?,
        val endTime: String?,
        val taskId: Int,
        val solverId: String)
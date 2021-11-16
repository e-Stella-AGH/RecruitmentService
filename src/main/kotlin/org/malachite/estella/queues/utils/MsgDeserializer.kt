package org.malachite.estella.queues.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.PayloadUUID
import org.springframework.stereotype.Component
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
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

    fun toInterviewResultRabbitDTO(msg: String): InterviewResultRabbitDTO? {
        return try {
            Base64.getDecoder()
                .decode(msg)
                .decodeToString()
                .let { Json.decodeFromString<InterviewResultRabbit>(it) }
                .let { it.toInterviewResultRabbitDTO() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("DBG: Bad request. Couldn't parse msg to InterviewResult: $msg")
            null
        }
    }


}

@Serializable
data class TaskResultRabbitDTO(
    val results: String,
    val code: String,
    val taskId: Int,
    val solverId: String
)

@Serializable
data class InterviewResultRabbit(
    val meetingUUID: String,
    val meetingDate: Long,
    val meetingLength: Int
) {
    fun toInterviewResultRabbitDTO(): InterviewResultRabbitDTO =
        InterviewResultRabbitDTO(
            PayloadUUID(meetingUUID).toUUID(),
            Instant.ofEpochMilli(meetingDate).let { Timestamp.from(it) },
            meetingLength
        )
}

data class InterviewResultRabbitDTO(
    val meetingUUID: UUID,
    val meetingDate: Timestamp,
    val meetingLength: Int
)
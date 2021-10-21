package org.malachite.estella.queues.utils

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.services.TaskService
import org.malachite.estella.services.TaskStageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

@Component
class MsgDeserializer(
        @Autowired private val taskService: TaskService,
        @Autowired private val taskStageService: TaskStageService) {

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
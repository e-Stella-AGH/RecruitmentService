package org.malachite.estella.queues.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.swagger.v3.core.util.ObjectMapperFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.malachite.estella.commons.models.tasks.TaskResult
import org.malachite.estella.process.domain.toTask
import org.malachite.estella.services.TaskService
import org.malachite.estella.services.TaskStageService
import org.springframework.beans.factory.annotation.Autowired
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

class MsgDeserializer(
        @Autowired private val taskService: TaskService,
        @Autowired private val taskStageService: TaskStageService) {

        fun toTaskResult(msg: String): TaskResult? {
            try {
                val decodedMsg = Json.decodeFromString<Map<String, String>>(msg)
                val taskStage = taskStageService.getTaskStage(decodedMsg["solverId"] as String)
                val task = taskService.getTaskById(decodedMsg["taskId"]!!.toInt()).toTask()
                val startTime = if (decodedMsg["startTime"].equals("null")) null else Timestamp.valueOf(decodedMsg["startTime"] as String)
                val endTime = if (decodedMsg["endTime"].equals("null")) null else Timestamp.valueOf(decodedMsg["endTime"] as String)
                return TaskResult(null,
                        SerialBlob(Base64.getEncoder().encode((decodedMsg["results"] as String).encodeToByteArray())),
                        SerialClob((decodedMsg["code"] as String).toCharArray()),
                        startTime,
                        endTime,
                        task,
                        taskStage
                )
            } catch (ex: Exception) {
                println("DBG: Bad request. Couldn't parse msg to TaskResult: $msg")
                return null
            }
        }



}
package org.malachite.estella.process.api

import com.fasterxml.jackson.annotation.JsonFormat
import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.malachite.estella.process.domain.toRecruitmentProcessDto
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.RecruitmentStageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.Date

@RestController
@RequestMapping("/api/process")
class RecruitmentProcessController(
    @Autowired private val processService: RecruitmentProcessService,
    @Autowired private val stageService: RecruitmentStageService
) {

    @CrossOrigin
    @GetMapping
    fun getAllProcesses(): ResponseEntity<List<RecruitmentProcessDto>> =
        processService.getProcesses()
            .let { it.map { it.toRecruitmentProcessDto() } }
            .toList()
            .let { ResponseEntity.ok(it) }

    @Transactional
    @CrossOrigin
    @GetMapping("/{processId}")
    fun getProcessById(@PathVariable("processId") processId: Int) =
        processService.getProcess(processId)
            .toRecruitmentProcessDto()
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @PutMapping("/stages/{processId}")
    fun updateStagesList(
        @PathVariable("processId") processId: Int,
        @RequestBody stages: UpdateStagesListRequest
    ) =
        processService.updateStagesList(processId, stages.stages)
            .let { OwnResponses.SUCCESS }

    @CrossOrigin
    @GetMapping("/stages")
    fun getAllStagesTypes() =
        stageService.getAllStagesTypes()
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @PutMapping("/{processId}/end_date")
    fun updateEndDate(
        @PathVariable("processId") processId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody dateRequest: DateRequest
    ) =
        processService.updateEndDate(jwt, processId, dateRequest.date).let {
            OwnResponses.SUCCESS
        }

    data class UpdateStagesListRequest(
        val stages: List<String>
    )

    data class DateRequest(
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
        val date: Date
    )
}
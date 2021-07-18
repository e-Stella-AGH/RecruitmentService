package org.malachite.estella.process.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.OwnResponses
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.process.domain.RecruitmentProcessDto
import org.malachite.estella.process.domain.toDto
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/process")
class RecruitmentProcessController(
    @Autowired private val processService: RecruitmentProcessService
) {

    @CrossOrigin
    @GetMapping
    fun getAllProcesses(): ResponseEntity<List<RecruitmentProcessDto>> =
        processService.getProcesses()
        .let { it.map { it.toDto() } }
            .toList()
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @GetMapping("/{processId}")
    fun getProcessById(@PathVariable("processId") processId: Int) =
        processService.getProcess(processId)
            .toDto()
            .let { ResponseEntity.ok(it) }

    @CrossOrigin
    @PutMapping("/stages/{processId}")
    fun updateStagesList(
        @PathVariable("processId") processId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody stages: UpdateStagesListRequest
    ) =
        processService.updateStagesList(jwt, processId, stages.stages)
            .let{ OwnResponses.SUCCESS }

    data class UpdateStagesListRequest(
        val stages: List<String>
    )
}
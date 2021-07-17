package org.malachite.estella.process.api

import org.malachite.estella.commons.EStellaHeaders
import org.malachite.estella.commons.models.offers.RecruitmentProcess
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
    fun getAllProcesses(): ResponseEntity<List<RecruitmentProcess>> =
        processService.getProcesses().let {
            ResponseEntity.ok(it.toList())
        }

    @CrossOrigin
    @GetMapping("/{processId}")
    fun getProcessById(@PathVariable("processId") processId: Int) =
        processService.getProcess(processId).let {
            ResponseEntity.ok(it)
        }

    @CrossOrigin
    @PutMapping("/stages/{processId}")
    fun updateStagesList(
        @PathVariable("processId") processId: Int,
        @RequestHeader(EStellaHeaders.jwtToken) jwt: String?,
        @RequestBody stages: UpdateStagesListRequest
    ) {
        processService.updateStagesList(jwt, processId, stages.stages)
    }

    data class UpdateStagesListRequest(
        val stages: List<String>
    )
}
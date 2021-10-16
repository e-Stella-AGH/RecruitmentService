package org.malachite.estella.services

import org.malachite.estella.commons.EStellaService
import org.malachite.estella.commons.UnauthenticatedException
import org.malachite.estella.commons.models.offers.*
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.process.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.Instant

@Service
class RecruitmentProcessService(
    @Autowired private val recruitmentProcessRepository: RecruitmentProcessRepository,
    @Autowired private val securityService: SecurityService,
    @Autowired private val recruitmentStageService: RecruitmentStageService
) : EStellaService<RecruitmentProcess>() {

    override val throwable: Exception = ProcessNotFoundException()

    fun getProcesses(): MutableIterable<RecruitmentProcess> =
        recruitmentProcessRepository.findAll()

    fun getProcess(id: Int): RecruitmentProcess =
        withExceptionThrower { recruitmentProcessRepository.findById(id).get() }

    fun addProcess(process: RecruitmentProcess): RecruitmentProcess = recruitmentProcessRepository.save(process)

    fun updateProcess(id: Int, process: RecruitmentProcess) {
        val currProcess: RecruitmentProcess = getProcess(id)
        if (process.isStarted()) throw ProcessAlreadyStartedException(id)
        val updated: RecruitmentProcess = currProcess.copy(
            id = process.id,
            startDate = process.startDate,
            endDate = process.endDate,
            offer = process.offer,
            stages = process.stages,
        )
        recruitmentProcessRepository.save(updated)
    }

    fun deleteProcess(id: Int) = recruitmentProcessRepository.deleteById(id)

    fun addBasicProcess(offer: Offer) {
        val recruitmentProcess = RecruitmentProcess(
            offer = offer,
            stages = listOf(RecruitmentStage(null, StageType.APPLIED), RecruitmentStage(null, StageType.ENDED))
        )
        recruitmentProcessRepository.save(recruitmentProcess)
    }

    fun getProcessFromStage(recruitmentStageId: Int): RecruitmentProcess =
        recruitmentProcessRepository.findAll()
            .find { it.stages.map { it.id }.contains(recruitmentStageId) }!!

    fun getProcessFromStage(applicationStage: ApplicationStageData): RecruitmentProcess =
        recruitmentProcessRepository.findAll()
            .find { it.stages.map { it.id }.contains(applicationStage.stage.id) }!!


    fun updateStagesList(processId: Int, stagesList: List<String>) {
        val user = securityService.getHrPartnerFromContext()
        val process = getProcess(processId)
        if (process.offer.creator.id != user?.id) throw UnauthenticatedException()
        if (process.isStarted()) throw ProcessAlreadyStartedException(processId)
        val stages = compareAndGetStagesList(process.stages, stagesList.toListOfRecruitmentStage())
        recruitmentProcessRepository.save(process.copy(stages = stages))
    }

    private fun compareAndGetStagesList(
        oldStages: List<RecruitmentStage>,
        newStages: List<RecruitmentStage>
    ): List<RecruitmentStage> {
        if (newStages[0].type != StageType.APPLIED || newStages.last().type != StageType.ENDED) throw InvalidStagesListException()
        val stages: MutableList<RecruitmentStage> = mutableListOf()
        stages += oldStages.zip(newStages).map {
            if (it.first.type != it.second.type)
                recruitmentStageService.save(it.first.copy(type = it.second.type))
            else it.first
        }
        oldStages.drop(newStages.size).map { recruitmentStageService.delete(it) }
        stages += newStages.drop(oldStages.size).map { recruitmentStageService.save(it) }
        if (stages.count { it.type == StageType.APPLIED } > 1 || stages.count { it.type == StageType.ENDED } > 1)
            throw InvalidStagesListException("There must be only one APPLIED and ENDED stage")
        return stages
    }

    private fun List<String>.toListOfRecruitmentStage() =
        this.toMutableList()
            .map {
                try {
                    StageType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    throw NoSuchStageTypeException(it)
                }
            }
            .map { RecruitmentStage(null, it) }

    fun updateEndDate(processId: Int, endDate: Date) {
        val userFromJWT = securityService.getHrPartnerFromContext()
        val process = getProcess(processId)
        assertCanPerformOperation(process, userFromJWT)
        if (process.isStarted()) throw ProcessAlreadyStartedException(processId)
        if (process.startDate == null && Date.from(Instant.now()).after(endDate)) throw InvalidEndDateException()
        if (process.startDate?.after(endDate) == true) throw InvalidEndDateException()
        recruitmentProcessRepository.save(process.copy(endDate = endDate))
    }

    fun startProcess(processId: Int) {
        val user = securityService.getHrPartnerFromContext()
        val process = getProcess(processId)
        if (process.startDate != null) throw ProcessAlreadyStartedException(processId)
        assertCanPerformOperation(process, user)
        recruitmentProcessRepository.save(process.copy(startDate = Date(Date.from(Instant.now()).time)))
    }

    private fun assertCanPerformOperation(process: RecruitmentProcess, user: HrPartner?) {
        if (process.offer.creator.id != user?.id) throw UnauthenticatedException()
    }

}
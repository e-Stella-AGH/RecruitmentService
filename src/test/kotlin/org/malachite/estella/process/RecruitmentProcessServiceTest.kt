package org.malachite.estella.process

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.malachite.estella.commons.loader.FakeRecruitmentProcess.getProcesses
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.people.HrPartner
import org.malachite.estella.commons.models.people.Organization
import org.malachite.estella.commons.models.people.User
import org.malachite.estella.process.domain.InvalidStagesListException
import org.malachite.estella.services.RecruitmentProcessService
import org.malachite.estella.services.RecruitmentStageService
import org.malachite.estella.services.SecurityService
import org.malachite.estella.util.offersWithNullProcess
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class RecruitmentProcessServiceTest {

    private val stagesRepository = DummyStageRepository()
    private val processRepository = DummyProcessRepository(stagesRepository)
    private val stageService = RecruitmentStageService(stagesRepository)
    private val securityMock = mockk<SecurityService>()
    private val service = RecruitmentProcessService(processRepository, securityMock, stageService)

    @BeforeEach
    fun setup() {
        processRepository.clear()
        stagesRepository.clear()
        every { securityMock.getHrPartnerFromContext() } returns HrPartner(
            null,
            Organization(null, "xd", User(1, "xd", "xd", "xd")),
            User(1, "xd", "xd", "xd")
        )
        processRepository.save(process)
        process.stages.forEach { stagesRepository.save(it) }
    }

    @Test
    fun `should be able to update stages when they are the same length`() {
        service.updateStagesList(
            process.id!!,
            listOf("APPLIED", "HR_INTERVIEW", "ENDED"))
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.HR_INTERVIEW),
            RecruitmentStage(2, StageType.ENDED)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(3)
    }

    @Test
    fun `should be able to add new stage while updating`() {
        service.updateStagesList(
            process.id!!,
            listOf("APPLIED", "HR_INTERVIEW", "TECHNICAL_INTERVIEW", "TECHNICAL_INTERVIEW", "ENDED")
        )
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.HR_INTERVIEW),
            RecruitmentStage(2, StageType.TECHNICAL_INTERVIEW),
            RecruitmentStage(3, StageType.TECHNICAL_INTERVIEW),
            RecruitmentStage(4, StageType.ENDED)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(5)
    }

    @Test
    fun `should be able to delete stage while updating`() {
        service.updateStagesList(
            process.id!!,
            listOf("APPLIED", "ENDED")
        )
        val updatedProcess = service.getProcess(process.id!!)
        expectThat(updatedProcess.stages).isEqualTo(listOf(
            RecruitmentStage(0, StageType.APPLIED),
            RecruitmentStage(1, StageType.ENDED)
        ))
        expectThat(updatedProcess.stages.size).isEqualTo(2)
    }

    @Test
    fun `should throw exception, when there's no APPLIED stage`() {
        expectThrows<InvalidStagesListException> {
            service.updateStagesList(
                process.id!!,
                listOf("HR_INTERVIEW", "ENDED")
            )
        }
    }

    @Test
    fun `should throw exception, when there's no ENDED stage`() {
        expectThrows<InvalidStagesListException> {
            service.updateStagesList(
                process.id!!,
                listOf("APPLIED", "HR_INTERVIEW")
            )
        }
    }

    @Test
    fun `should throw exception, when there's more than one APPLIED or ENDED stage`() {
        expectThrows<InvalidStagesListException> {
            service.updateStagesList(
                process.id!!,
                listOf("APPLIED", "ENDED", "HR_INTERVIEW", "APPLIED", "ENDED")
            )
        }
    }

    private val process = getProcesses(offersWithNullProcess)[0].let {
        it.copy(stages = it.stages.mapIndexed { index, recruitmentStage -> recruitmentStage.copy(id = index) })
    }
}
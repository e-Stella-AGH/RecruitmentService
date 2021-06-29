package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.quizes.Quiz
import org.malachite.estella.commons.models.tasks.Task
import java.sql.Date
import java.time.LocalDate

object FakeRecruitmentProcess {

    private fun getRandomNumber(num: Int = 120): Long {
        return (0..num).random().toLong()
    }

    data class ProcessPayload(
        val startDate: Date = Date.valueOf(LocalDate.now().minusDays(getRandomNumber())),
        val endDate: Date = Date.valueOf(LocalDate.now().plusDays(getRandomNumber())),
        val recruitmentStages: List<RecruitmentStage>,
        val tasks: List<Task> = listOf(), val quizzes: List<Quiz> = listOf()
    ) {
        fun toRecruitmentProcess(offer: Offer): RecruitmentProcess {
            return RecruitmentProcess(
                offer.id, startDate, endDate, offer, recruitmentStages,
                quizzes.toSet(), tasks.toSet()
            )
        }
    }

    fun recruitmentStages() = listOf(
        RecruitmentStage(null, StageType.APPLIED),
        RecruitmentStage(null, StageType.HR_INTERVIEW),
        RecruitmentStage(null, StageType.TECHNICAL_INTERVIEW),
        RecruitmentStage(null, StageType.TASK),
        RecruitmentStage(null, StageType.QUIZ),
        RecruitmentStage(null, StageType.ENDED)
    )

    fun getProcesses(offers: List<Offer>): List<RecruitmentProcess> {
        return offers.map {
            ProcessPayload(
                recruitmentStages = recruitmentStages().subList(0, 3)
            ).toRecruitmentProcess(it)
        }
    }

}

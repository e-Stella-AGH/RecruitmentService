package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.offers.Offer
import org.malachite.estella.commons.models.offers.RecruitmentProcess
import org.malachite.estella.commons.models.offers.RecruitmentStage
import org.malachite.estella.commons.models.offers.StageType
import org.malachite.estella.commons.models.quizes.Quiz
import org.malachite.estella.commons.models.tasks.Task
import java.sql.Date

object FakeRecruitmentProcess {

    private fun getRandomNumber(num: Int = 120): Long {
        return (0..num).random().toLong()
    }

    data class ProcessPayload(
        val startDate: Date? = null,
        val endDate: Date? = null,
        val recruitmentStages: List<RecruitmentStage>,
        val tasks: List<Task> = listOf(), val quizzes: List<Quiz> = listOf()
    ) {
        fun toRecruitmentProcess(offer: Offer): RecruitmentProcess {
            return RecruitmentProcess(
                offer.id, startDate, endDate, offer, recruitmentStages.toSet()
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
                recruitmentStages = recruitmentStages().subList(0, 4) + recruitmentStages().last()
            ).toRecruitmentProcess(it)
        }
    }

}

package org.malachite.estella.commons.loader

import org.malachite.estella.commons.models.tasks.Task
import org.malachite.estella.process.domain.TaskDto
import org.malachite.estella.process.domain.toTask
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant
import java.util.*

object FakeTasks {

    private val encodedTestsFile: String = Files
        .readAllBytes(Paths.get("src/main/kotlin/org/malachite/estella/commons/loader/fakeTests.json"))
        .let { Base64.getEncoder().encodeToString(it) }

    private val encodedDescriptionFile: String = Files
        .readAllBytes(Paths.get("src/main/kotlin/org/malachite/estella/commons/loader/fakeTaskDescription.md"))
        .let { Base64.getEncoder().encodeToString(it) }

    val task = TaskDto(
        id = null,
        testsBase64 = encodedTestsFile,
        descriptionFileName = "fakeTaskDescription.md",
        descriptionBase64 = encodedDescriptionFile,
        timeLimit = 30,
        deadline = Timestamp.from(Instant.now())
    ).toTask()
}
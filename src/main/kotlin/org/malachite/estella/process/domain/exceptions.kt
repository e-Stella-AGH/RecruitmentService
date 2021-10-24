package org.malachite.estella.process.domain

class ProcessNotFoundException: Exception()
class NoSuchStageTypeException(val type: String): Exception()
class InvalidStagesListException(val error: String = "Stages list must start with APPLIED and end with ENDED"): Exception()
class InvalidDateException: Exception()
class ProcessAlreadyStartedException(processId: Int): Exception("Process with id: $processId has already been started, therefore it cannot be modified anymore!")
class ProcessNotStartedException: Exception("This process hasn't started yet! You cannot apply on this offer!")
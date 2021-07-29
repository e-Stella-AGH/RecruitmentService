package org.malachite.estella.process.domain

class NoSuchStageTypeException(val type: String): Exception()
class InvalidStagesListException(val error: String = "Stages list must start with APPLIED and end with ENDED"): Exception()
class InvalidEndDateException: Exception()
package org.malachite.estella.commons

import org.malachite.estella.interview.domain.InvalidUUIDException
import java.util.*


data class PayloadUUID(val uuid: String) {
    fun toUUID(): UUID {
        try {
            return UUID.fromString(uuid)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            throw InvalidUUIDException()
        }
    }
}


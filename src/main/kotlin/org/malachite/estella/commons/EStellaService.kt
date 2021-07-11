package org.malachite.estella.commons

abstract class EStellaService {

    abstract val throwable: Exception

    fun withExceptionThrower(fn: () -> Any) =
        try {
            fn()
        } catch(ex: NoSuchElementException) {
            throw throwable
        }
}
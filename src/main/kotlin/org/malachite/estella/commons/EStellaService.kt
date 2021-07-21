package org.malachite.estella.commons

abstract class EStellaService<T> {

    abstract val throwable: Exception

    fun withExceptionThrower(fn: () -> T) =
        try {
            fn()
        } catch(ex: NoSuchElementException) {
            throw throwable
        }
}
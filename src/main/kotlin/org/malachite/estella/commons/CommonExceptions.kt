package org.malachite.estella.commons

class UnauthenticatedException : Exception()
class DataViolationException(val msg: String) : Exception()
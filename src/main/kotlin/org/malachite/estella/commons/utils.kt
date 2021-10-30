package org.malachite.estella.commons

import java.sql.Blob
import java.sql.Clob
import java.util.*
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

fun Clob?.toBase64String(): String =
    this?.let {
        Base64.getEncoder()
            .encodeToString(this.getSubString(1, this.length().toInt()).toByteArray())
    } ?: ""

fun Blob.toBase64String(): String =
    Base64.getEncoder().encodeToString(this.getBytes(1, this.length().toInt()))

fun String.toClob(): Clob =
    SerialClob(String(Base64.getDecoder().decode(this)).toCharArray())

fun String.toBlob(): Blob =
    SerialBlob(String(Base64.getDecoder().decode(this)).toByteArray())

fun String.decodeBase64(): String =
    String(Base64.getDecoder().decode(this))

fun String.encodeToBase64(): String =
    String(Base64.getEncoder().encode(this.toByteArray()))
package org.malachite.estella.commons

enum class Permission {
    CREATE, UPDATE, DELETE, READ;

    companion object {
        fun allPermissions() = setOf(CREATE, UPDATE, DELETE, READ)
    }
}
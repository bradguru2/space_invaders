package org.game.invaders.utilities

fun <T:Any> T.loadResourceString(path: String): String =
    requireNotNull(this.javaClass.getResourceAsStream(path)) {
        "Resource not found: $path"
    }.bufferedReader().use { it.readText() }
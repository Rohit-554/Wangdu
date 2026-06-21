package io.jadu.wangdu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

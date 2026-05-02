package dev.teogor.traverse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
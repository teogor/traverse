package dev.teogor.traverse.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
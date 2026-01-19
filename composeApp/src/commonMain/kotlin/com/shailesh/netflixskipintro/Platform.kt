package com.shailesh.netflixskipintro

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
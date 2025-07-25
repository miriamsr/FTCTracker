package me.zharel.ftctracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
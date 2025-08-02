package me.zharel.ftctracker

import java.io.File
import java.net.URI
import kotlin.system.exitProcess

data class Release(val tag: String, val assetUrl: String)

object Updater {
    private const val GITHUB_API_RELEASES = "https://api.github.com/repos/FTC-Tracker/FTCTracker/releases/latest"
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    fun checkForUpdate() {
        val release = fetchLatestRelease() ?: return
        val current = AppVersion.VERSION.removePrefix("v")

        if (release.tag.removePrefix("v") == current) {
            println("✅ Already up to date (version $current)")
            return
        }

        println("⬇️ Update available: ${release.tag} (current: $current)")
        downloadAndLaunchInstaller(release.assetUrl)
        exitProcess(0)
    }

    private fun fetchLatestRelease(): Release? = try {
        val json = URI.create(GITHUB_API_RELEASES).toURL().readText()
        val tag = Regex("\"tag_name\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: return null

        val assetRegex = if (isWindows)
            "\"browser_download_url\":\\s*\"([^\"]+server-windows\\.exe)\""
        else
            "\"browser_download_url\":\\s*\"([^\"]+server-macos\\.dmg)\""

        val assetUrl = Regex(assetRegex).find(json)?.groupValues?.get(1) ?: return null

        Release(tag, assetUrl)
    } catch (e: Exception) {
        println("⚠️ Failed to check for updates: ${e.message}")
        null
    }

    private fun downloadAndLaunchInstaller(url: String) {
        val suffix = if (isWindows) ".exe" else ".dmg"
        val installer = File.createTempFile("server-update", suffix)

        println("⬇️ Downloading update from $url")
        URI.create(url).toURL().openStream().use { it.copyTo(installer.outputStream()) }

        println("🚀 Launching installer: ${installer.absolutePath}")
        if (isWindows) {
            ProcessBuilder("cmd", "/c", installer.absolutePath).start()
        } else {
            ProcessBuilder("open", installer.absolutePath).start()
        }
    }
}

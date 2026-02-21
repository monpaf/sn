package app.morphe.patches.standardnotes

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch

private object ResAnchor

@Suppress("unused")
val iconPatch = resourcePatch(
    name = "Replace App Icon",
    description = "Replaces the app icon with a custom one"
) {

    compatibleWith("com.standardnotes")

    execute {
        val densities = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

        for (dpi in densities) {
            replaceFromResourceIfExists(
                apkPath = "res/mipmap-$dpi/ic_launcher.png",
                resourcePath = "icons/mipmap-$dpi/ic_launcher.png"
            )
            replaceFromResourceIfExists(
                apkPath = "res/mipmap-$dpi/ic_launcher_background.png",
                resourcePath = "icons/mipmap-$dpi/ic_launcher_background.png"
            )
        }
    }
}

private fun ResourcePatchContext.replaceFromResourceIfExists(apkPath: String, resourcePath: String) {
    val outFile = runCatching { this[apkPath] }.getOrNull() ?: return
    if (!outFile.exists()) return
    outFile.writeBytes(readBundledBytes(resourcePath))
}

private fun readBundledBytes(resourcePath: String): ByteArray {
    val cl = ResAnchor::class.java.classLoader
    val stream =
        cl?.getResourceAsStream(resourcePath)
            ?: ResAnchor::class.java.getResourceAsStream("/$resourcePath")
    return stream?.use { it.readBytes() } ?: error("Missing bundled resource: $resourcePath")
}

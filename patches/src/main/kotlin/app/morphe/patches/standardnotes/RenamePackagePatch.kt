package app.morphe.patches.standardnotes

import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val renamePackagePatch = resourcePatch(
    name = "Change Package Name",
    description = "Changes the app package name in AndroidManifest.xml"
) {
    
    compatibleWith("com.standardnotes")
    
    execute {
        val oldPkg = "com.standardnotes"
        val newPkg = "com.standardnotesmod"

        val manifestFile = this["AndroidManifest.xml"]
        val original = manifestFile.readText(Charsets.UTF_8)
        val updated = rewriteManifest(original, oldPkg, newPkg)

        if (updated != original) {
            manifestFile.writeText(updated, Charsets.UTF_8)
        }
    }
}

private fun rewriteManifest(manifest: String, oldPkg: String, newPkg: String): String {
    var s = manifest

    s = s.replace(
        Regex("""\bandroid:name\s*=\s*"\."""),
        """android:name="$oldPkg."""
    )

    s = s.replace(
        Regex("""\bpackage\s*=\s*"${Regex.escape(oldPkg)}""""),
        """package="$newPkg""""
    )

    s = s.replace(
        Regex("""android:authorities\s*=\s*"${Regex.escape(oldPkg)}([^"]*)""""),
        """android:authorities="$newPkg$1""""
    )

    s = s.replace(
        Regex("""android:taskAffinity\s*=\s*"${Regex.escape(oldPkg)}([^"]*)""""),
        """android:taskAffinity="$newPkg$1""""
    )

    s = s.replace(
        Regex("""android:name\s*=\s*"${Regex.escape(oldPkg)}\.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION""""),
        """android:name="$newPkg.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION""""
    )

    return s
}

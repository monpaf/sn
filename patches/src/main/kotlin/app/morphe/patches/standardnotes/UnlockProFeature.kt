package app.morphe.patches.standardnotes

import app.morphe.patcher.patch.rawResourcePatch

private const val APP_JS_PATH = "assets/Web.bundle/src/web-src/app.js"

private const val MARKER_1 = ".OfflineUserRoles,void 0,[]),"
private const val INSERT_1 = "this.offlineRoles.includes(r.RoleName.NAMES.ProUser)||this.offlineRoles.push(r.RoleName.NAMES.ProUser),"

private const val MARKER_2 = " this.onlineRolesIncludePaidSubscription()||this.hasOfflineRepo()||this.hasFirstPartyOnlineSubscription()}"
private const val REPLACE_2 = "!0}"

private const val MARKER_3 = """const e=this.getOfflineRepo();return!(!e||!e.content.offlineFeaturesUrl||e.content.offlineFeaturesUrl!==this.PROD_OFFLINE_FEATURES_URL&&"localhost"!==new URL(e.content.offlineFeaturesUrl).hostname)"""
private const val REPLACE_3 = "return!0"

private const val MARKER_4 = "setOnlineRoles(e){"
private const val INSERT_4 = "e.includes(r.RoleName.NAMES.ProUser)||e.push(r.RoleName.NAMES.ProUser);"

private const val MARKER_5 = "setOfflineRoles(e){"
private const val INSERT_5 = "e.includes(r.RoleName.NAMES.ProUser)||e.push(r.RoleName.NAMES.ProUser);"

private const val MARKER_6 = "this.getThirdPartyFeatureStatus(e.featureId)"
private const val REPLACE_6 = "c.iKc.Entitled"

private const val MARKER_7 = "?c.iKc.Entitled:c.iKc.NoUserSubscription"
private const val REPLACE_7 = "?c.iKc.Entitled:c.iKc.Entitled"

private const val MARKER_8 = "&&void 0!==e.inContextOfItem.shared_vault_uuid"
private const val REPLACE_8 = ""

private const val MARKER_9 = "return c.iKc.NoUserSubscription"
private const val REPLACE_9 = "return c.iKc.Entitled"

private const val MARKER_10 = "c.iKc.NotInCurrentPlan"
private const val REPLACE_10 = "c.iKc.Entitled"

private const val MARKER_11 = "e?c.iKc.InCurrentPlanButExpired"
private const val REPLACE_11 = "e?c.iKc.Entitled"

private const val MARKER_12 = "c.iKc.InCurrentPlanButExpired:c.iKc.Entitled:c.iKc.NoUserSubscription"  
private const val REPLACE_12 = "c.iKc.Entitled:c.iKc.Entitled:c.iKc.Entitled"

private sealed class Operation {
    data class Insert(val marker: String, val insert: String) : Operation()
    data class Replace(val target: String, val replacement: String) : Operation()
}

@Suppress("unused")
val webBundleAppJsPatch = rawResourcePatch(
    name = "Unlock Pro Features",
    description = "Unlock all features from the pro subscription"
) {

    compatibleWith("com.standardnotes")

    execute {
        val appJsFile = get(APP_JS_PATH)
        var text = appJsFile.readText(Charsets.UTF_8)

        val operations = listOf(
            Operation.Insert(MARKER_1, INSERT_1),
            Operation.Replace(MARKER_2, REPLACE_2),
            Operation.Replace(MARKER_3, REPLACE_3),
            Operation.Insert(MARKER_4, INSERT_4),
            Operation.Insert(MARKER_5, INSERT_5),
            Operation.Replace(MARKER_6, REPLACE_6),
            Operation.Replace(MARKER_7, REPLACE_7),
            Operation.Replace(MARKER_8, REPLACE_8),
            Operation.Replace(MARKER_9, REPLACE_9),
            Operation.Replace(MARKER_10, REPLACE_10),
            Operation.Replace(MARKER_11, REPLACE_11),
            Operation.Replace(MARKER_12, REPLACE_12),
        )

        for (op in operations) {
            text = when (op) {
                is Operation.Insert ->
                    insertAfterUnique(text, op.marker, op.insert)

                is Operation.Replace ->
                    replaceUnique(text, op.target, op.replacement)
            }
        }

        appJsFile.writeText(text, Charsets.UTF_8)
    }
}

private fun insertAfterUnique(text: String, marker: String, toInsert: String): String {
    val first = text.indexOf(marker)
    require(first >= 0) { "Insert marker not found: $marker" }

    val second = text.indexOf(marker, first + marker.length)
    require(second == -1) { "Insert marker not unique: $marker" }

    val insertPos = first + marker.length
    return text.substring(0, insertPos) + toInsert + text.substring(insertPos)
}

private fun replaceUnique(text: String, target: String, replacement: String): String {
    val first = text.indexOf(target)
    require(first >= 0) { "Replace target not found: $target" }

    val second = text.indexOf(target, first + target.length)
    require(second == -1) { "Replace target not unique: $target" }

    return text.replaceFirst(target, replacement)
}

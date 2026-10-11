package com.samidevstudio.pocketdex.domain

/**
 * Names retained by the API/cache but not supported by PocketDex's regular type-browsing summaries.
 * Unknown future type names remain visible unless they are explicitly added here.
 */
object TypeBrowseability {
    private val unsupportedBrowseableNames = setOf("unknown", "stellar")

    fun isBrowseable(typeName: String): Boolean =
        typeName.trim().lowercase() !in unsupportedBrowseableNames
}

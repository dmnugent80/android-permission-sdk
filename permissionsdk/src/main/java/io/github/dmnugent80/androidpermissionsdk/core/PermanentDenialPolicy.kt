package io.github.dmnugent80.androidpermissionsdk.core

internal class PermanentDenialPolicy {
    fun isPermanentlyDenied(
        hasRequestOrEducationHistory: Boolean,
        shouldShowRationale: Boolean
    ): Boolean {
        return hasRequestOrEducationHistory && !shouldShowRationale
    }
}

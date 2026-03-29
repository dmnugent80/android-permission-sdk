package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Result of a permission request call.
 */
sealed interface PermissionResult {
    data object Granted : PermissionResult
    data class Denied(val canRequestAgain: Boolean) : PermissionResult
    data object Cancelled : PermissionResult
    data object AlreadyInProgress : PermissionResult
    data object MissingFromManifest : PermissionResult
    data object UnavailableOnApiLevel : PermissionResult
}

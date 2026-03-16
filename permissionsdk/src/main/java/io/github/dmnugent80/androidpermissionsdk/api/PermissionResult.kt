package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Result of a permission request call.
 */
sealed interface PermissionResult {
    data object Granted : PermissionResult
    data object Denied : PermissionResult
    data object PermanentlyDenied : PermissionResult
    data object Cancelled : PermissionResult
}

package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Current effective status for an app permission.
 */
sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data object NotRequestedYet : PermissionStatus
    data object Denied : PermissionStatus
}

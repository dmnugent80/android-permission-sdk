package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Current effective status for an app permission.
 */
sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data object NotRequestedYet : PermissionStatus
    data class Denied(val canRequestAgain: Boolean) : PermissionStatus
    data object MissingFromManifest : PermissionStatus
    data object UnavailableOnApiLevel : PermissionStatus
    data object RequestInProgress : PermissionStatus
}

fun PermissionStatus.isRequestable(): Boolean = when (this) {
    PermissionStatus.Granted -> false
    PermissionStatus.NotRequestedYet -> true
    is PermissionStatus.Denied -> canRequestAgain
    PermissionStatus.MissingFromManifest -> false
    PermissionStatus.UnavailableOnApiLevel -> false
    PermissionStatus.RequestInProgress -> false
}

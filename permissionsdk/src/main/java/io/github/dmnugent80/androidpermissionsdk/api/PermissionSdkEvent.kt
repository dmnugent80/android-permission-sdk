package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Structured lifecycle events emitted during permission requests.
 */
sealed interface PermissionSdkEvent {
    val permission: AppPermission
    val timestampMillis: Long

    data class RequestStarted(
        override val permission: AppPermission,
        override val timestampMillis: Long = System.currentTimeMillis()
    ) : PermissionSdkEvent

    data class LauncherRegistrationFailed(
        override val permission: AppPermission,
        val exception: IllegalStateException,
        override val timestampMillis: Long = System.currentTimeMillis()
    ) : PermissionSdkEvent

    data class SystemResponseReceived(
        override val permission: AppPermission,
        val rawResult: Map<String, Boolean>,
        override val timestampMillis: Long = System.currentTimeMillis()
    ) : PermissionSdkEvent

    data class RequestCompleted(
        override val permission: AppPermission,
        val result: PermissionResult,
        override val timestampMillis: Long = System.currentTimeMillis()
    ) : PermissionSdkEvent
}

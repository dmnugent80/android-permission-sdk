package io.github.dmnugent80.androidpermissionsdk.platform

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkConfig
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkEvent
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkLogger
import io.github.dmnugent80.androidpermissionsdk.core.DiagnosticsEmitter

internal class DefaultDiagnosticsEmitter(
    private val config: PermissionSdkConfig
) : DiagnosticsEmitter {

    override fun emitRequestStarted(permission: AppPermission) {
        emit(PermissionSdkEvent.RequestStarted(permission))
        logDebug("Request started for $permission")
    }

    override fun emitLauncherRegistrationFailed(
        permission: AppPermission,
        exception: IllegalStateException
    ) {
        emit(PermissionSdkEvent.LauncherRegistrationFailed(permission, exception))
        log(
            PermissionSdkLogger.LogLevel.WARN,
            TAG,
            "Launcher registration failed for $permission",
            exception
        )
    }

    override fun emitSystemResponseReceived(
        permission: AppPermission,
        rawResult: Map<String, Boolean>
    ) {
        emit(PermissionSdkEvent.SystemResponseReceived(permission, rawResult))
        logDebug("System response for $permission: $rawResult")
    }

    override fun emitRequestCompleted(permission: AppPermission, result: PermissionResult) {
        emit(PermissionSdkEvent.RequestCompleted(permission, result))
        logDebug("Request completed for $permission: $result")
    }

    override fun log(
        level: PermissionSdkLogger.LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        runCatching {
            config.logger?.log(level, tag, message, throwable)
        }
    }

    private fun emit(event: PermissionSdkEvent) {
        runCatching {
            config.eventListener?.onEvent(event)
        }
    }

    private fun logDebug(message: String) {
        if (config.isDebugMode) {
            log(PermissionSdkLogger.LogLevel.DEBUG, TAG, message, null)
        }
    }

    private companion object {
        const val TAG = "PermissionSDK"
    }
}

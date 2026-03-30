package io.github.dmnugent80.androidpermissionsdk.platform

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkLogger
import io.github.dmnugent80.androidpermissionsdk.core.DiagnosticsEmitter

/**
 * No-op implementation for when diagnostics are disabled.
 * Avoids null checks throughout the codebase.
 */
internal object NoOpDiagnosticsEmitter : DiagnosticsEmitter {
    override fun emitRequestStarted(permission: AppPermission) = Unit
    override fun emitLauncherRegistrationFailed(permission: AppPermission, exception: IllegalStateException) = Unit
    override fun emitSystemResponseReceived(permission: AppPermission, rawResult: Map<String, Boolean>) = Unit
    override fun emitRequestCompleted(permission: AppPermission, result: PermissionResult) = Unit
    override fun log(level: PermissionSdkLogger.LogLevel, tag: String, message: String, throwable: Throwable?) = Unit
}

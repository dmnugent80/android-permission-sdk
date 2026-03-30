package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkLogger

/**
 * Internal interface for components to emit diagnostic events.
 */
internal interface DiagnosticsEmitter {
    fun emitRequestStarted(permission: AppPermission)
    fun emitLauncherRegistrationFailed(permission: AppPermission, exception: IllegalStateException)
    fun emitSystemResponseReceived(permission: AppPermission, rawResult: Map<String, Boolean>)
    fun emitRequestCompleted(permission: AppPermission, result: PermissionResult)

    fun log(level: PermissionSdkLogger.LogLevel, tag: String, message: String, throwable: Throwable?)
}

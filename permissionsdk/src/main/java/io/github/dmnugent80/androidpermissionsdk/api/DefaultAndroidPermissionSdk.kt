package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.core.DiagnosticsEmitter
import io.github.dmnugent80.androidpermissionsdk.core.PermissionApiLevelChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionManifestChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestTracker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import io.github.dmnugent80.androidpermissionsdk.platform.NoOpDiagnosticsEmitter

/**
 * Default SDK facade implementation.
 */
class DefaultAndroidPermissionSdk internal constructor(
    private val educationStore: PermissionEducationStore,
    private val requestCoordinator: PermissionRequestCoordinator,
    private val statusResolver: PermissionStatusResolver,
    private val resultResolver: PermissionResultResolver,
    private val apiLevelChecker: PermissionApiLevelChecker,
    private val manifestChecker: PermissionManifestChecker,
    private val requestTracker: PermissionRequestTracker,
    private val diagnostics: DiagnosticsEmitter = NoOpDiagnosticsEmitter
) : AndroidPermissionSdk {

    override fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus {
        return statusResolver.resolve(permission, activity)
    }

    override fun shouldShowEducation(permission: AppPermission): Boolean {
        return !educationStore.wasEducationShown(permission)
    }

    override fun markEducationShown(permission: AppPermission) {
        educationStore.markEducationShown(permission)
    }

    override fun isRequestInProgress(permission: AppPermission): Boolean {
        return requestTracker.isRequestInProgress(permission)
    }

    override suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): PermissionResult {
        preflightCheck(permission)?.let { result ->
            diagnostics.emitRequestCompleted(permission, result)
            return result
        }

        requestTracker.markRequestStarted(permission)
        try {
            diagnostics.emitRequestStarted(permission)
            val result = executeRequest(permission, activity)
            diagnostics.emitRequestCompleted(permission, result)
            return result
        } finally {
            requestTracker.markRequestCompleted(permission)
        }
    }

    private fun preflightCheck(permission: AppPermission): PermissionResult? {
        return when {
            !apiLevelChecker.isAvailableOnCurrentApiLevel(permission) ->
                PermissionResult.UnavailableOnApiLevel
            !manifestChecker.isDeclaredInManifest(permission) ->
                PermissionResult.MissingFromManifest
            requestTracker.isRequestInProgress(permission) ->
                PermissionResult.AlreadyInProgress
            else -> null
        }
    }

    private suspend fun executeRequest(
        permission: AppPermission,
        activity: ComponentActivity
    ): PermissionResult {
        val requestResult = requestCoordinator.request(permission, activity)
        diagnostics.emitSystemResponseReceived(permission, requestResult.toMap())
        if (requestResult.isEmpty()) {
            return PermissionResult.Cancelled
        }

        educationStore.markRequested(permission)
        return resultResolver.resolve(permission, activity, requestResult)
    }
}

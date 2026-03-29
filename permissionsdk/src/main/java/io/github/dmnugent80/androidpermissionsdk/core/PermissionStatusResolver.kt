package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus

internal class PermissionStatusResolver(
    private val permissionChecker: PermissionChecker,
    private val educationStore: PermissionEducationStore,
    private val grantHistoryStore: PermissionGrantHistoryStore,
    private val apiLevelChecker: PermissionApiLevelChecker,
    private val manifestChecker: PermissionManifestChecker,
    private val rationaleChecker: PermissionRationaleChecker,
    private val requestTracker: PermissionRequestTracker
) {
    fun resolve(permission: AppPermission, activity: Activity): PermissionStatus {
        return resolvePreflightError(permission)
            ?: resolveGrantedOrPending(permission, activity)
    }

    private fun resolvePreflightError(permission: AppPermission): PermissionStatus? {
        return when {
            !apiLevelChecker.isAvailableOnCurrentApiLevel(permission) ->
                PermissionStatus.UnavailableOnApiLevel
            !manifestChecker.isDeclaredInManifest(permission) ->
                PermissionStatus.MissingFromManifest
            requestTracker.isRequestInProgress(permission) ->
                PermissionStatus.RequestInProgress
            else -> null
        }
    }

    private fun resolveGrantedOrPending(
        permission: AppPermission,
        activity: Activity
    ): PermissionStatus {
        if (permissionChecker.isGranted(activity, permission)) {
            grantHistoryStore.markGranted(permission)
            return PermissionStatus.Granted
        }
        if (!educationStore.wasRequested(permission)) {
            return PermissionStatus.NotRequestedYet
        }
        val canRequestAgain = rationaleChecker.shouldShowRationale(activity, permission) ||
            grantHistoryStore.wasEverGranted(permission)
        return PermissionStatus.Denied(canRequestAgain)
    }
}

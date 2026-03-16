package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus

internal class PermissionStatusResolver(
    private val permissionChecker: PermissionChecker,
    private val rationaleChecker: RationaleChecker,
    private val educationStore: PermissionEducationStore,
    private val permanentDenialPolicy: PermanentDenialPolicy
) {
    fun resolve(permission: AppPermission, activity: Activity): PermissionStatus {
        if (permissionChecker.isGranted(activity, permission)) {
            return PermissionStatus.Granted
        }

        val hasHistory =
            educationStore.wasEducationShown(permission) || educationStore.wasRequested(permission)
        if (!hasHistory) {
            return PermissionStatus.NotRequestedYet
        }

        val shouldShowRationale = rationaleChecker.shouldShowRationale(activity, permission)
        return if (permanentDenialPolicy.isPermanentlyDenied(hasHistory, shouldShowRationale)) {
            PermissionStatus.PermanentlyDenied
        } else {
            PermissionStatus.Denied
        }
    }
}

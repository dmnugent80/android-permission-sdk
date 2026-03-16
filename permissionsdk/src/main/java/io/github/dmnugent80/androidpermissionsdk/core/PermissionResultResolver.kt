package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult

internal class PermissionResultResolver(
    private val permissionChecker: PermissionChecker,
    private val rationaleChecker: RationaleChecker,
    private val educationStore: PermissionEducationStore,
    private val permanentDenialPolicy: PermanentDenialPolicy
) {
    fun resolve(
        permission: AppPermission,
        activity: Activity,
        requestResult: Map<String, Boolean>
    ): PermissionResult {
        if (requestResult.isEmpty()) {
            return PermissionResult.Cancelled
        }

        if (permissionChecker.isGranted(activity, permission)) {
            return PermissionResult.Granted
        }

        val hasHistory =
            educationStore.wasEducationShown(permission) || educationStore.wasRequested(permission)
        val shouldShowRationale = rationaleChecker.shouldShowRationale(activity, permission)

        return if (permanentDenialPolicy.isPermanentlyDenied(hasHistory, shouldShowRationale)) {
            PermissionResult.PermanentlyDenied
        } else {
            PermissionResult.Denied
        }
    }
}

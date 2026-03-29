package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult

internal class PermissionResultResolver(
    private val permissionChecker: PermissionChecker,
    private val rationaleChecker: PermissionRationaleChecker
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

        val canRequestAgain = rationaleChecker.shouldShowRationale(activity, permission)
        return PermissionResult.Denied(canRequestAgain)
    }
}

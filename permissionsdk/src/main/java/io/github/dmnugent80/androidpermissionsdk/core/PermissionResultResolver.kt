package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult

internal class PermissionResultResolver(
    private val permissionChecker: PermissionChecker
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

        return PermissionResult.Denied
    }
}

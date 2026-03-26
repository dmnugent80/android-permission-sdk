package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus

internal class PermissionStatusResolver(
    private val permissionChecker: PermissionChecker,
    private val educationStore: PermissionEducationStore
) {
    fun resolve(permission: AppPermission, activity: Activity): PermissionStatus {
        if (permissionChecker.isGranted(activity, permission)) {
            return PermissionStatus.Granted
        }

        val hasRequestHistory = educationStore.wasRequested(permission)
        if (!hasRequestHistory) {
            return PermissionStatus.NotRequestedYet
        }

        return PermissionStatus.Denied
    }
}

package io.github.dmnugent80.androidpermissionsdk.platform

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionChecker

internal class AndroidPermissionChecker : PermissionChecker {
    override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
        return permission.androidPermissions.all { androidPermission ->
            ContextCompat.checkSelfPermission(
                activity,
                androidPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

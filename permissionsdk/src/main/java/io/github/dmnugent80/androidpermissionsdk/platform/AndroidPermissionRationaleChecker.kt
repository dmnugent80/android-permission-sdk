package io.github.dmnugent80.androidpermissionsdk.platform

import android.app.Activity
import androidx.core.app.ActivityCompat
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRationaleChecker

internal class AndroidPermissionRationaleChecker : PermissionRationaleChecker {

    override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
        return permission.androidPermissions.any { androidPermission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermission)
        }
    }
}

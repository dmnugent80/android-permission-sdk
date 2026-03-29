package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionRationaleChecker {
    fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean
}

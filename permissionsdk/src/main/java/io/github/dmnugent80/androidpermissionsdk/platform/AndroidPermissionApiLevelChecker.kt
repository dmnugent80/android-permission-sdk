package io.github.dmnugent80.androidpermissionsdk.platform

import android.os.Build
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionApiLevelChecker

internal class AndroidPermissionApiLevelChecker : PermissionApiLevelChecker {

    override fun isAvailableOnCurrentApiLevel(permission: AppPermission): Boolean {
        val minApiLevel = permission.minApiLevel ?: return true
        return Build.VERSION.SDK_INT >= minApiLevel
    }
}

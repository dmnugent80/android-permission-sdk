package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionApiLevelChecker {
    fun isAvailableOnCurrentApiLevel(permission: AppPermission): Boolean
}

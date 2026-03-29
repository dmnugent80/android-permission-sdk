package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionGrantHistoryStore {
    fun wasEverGranted(permission: AppPermission): Boolean
    fun markGranted(permission: AppPermission)
}

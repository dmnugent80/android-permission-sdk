package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionRequestTracker {
    fun isRequestInProgress(permission: AppPermission): Boolean
    fun markRequestStarted(permission: AppPermission)
    fun markRequestCompleted(permission: AppPermission)
}

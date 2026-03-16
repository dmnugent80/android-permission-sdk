package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionEducationStore {
    fun wasEducationShown(permission: AppPermission): Boolean

    fun markEducationShown(permission: AppPermission)

    fun wasRequested(permission: AppPermission): Boolean

    fun markRequested(permission: AppPermission)
}

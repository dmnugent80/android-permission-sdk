package io.github.dmnugent80.androidpermissionsdk.core

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionManifestChecker {
    fun isDeclaredInManifest(permission: AppPermission): Boolean
}

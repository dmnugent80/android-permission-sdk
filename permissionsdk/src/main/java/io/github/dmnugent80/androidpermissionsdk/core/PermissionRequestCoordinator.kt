package io.github.dmnugent80.androidpermissionsdk.core

import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission

internal interface PermissionRequestCoordinator {
    suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): Map<String, Boolean>
}

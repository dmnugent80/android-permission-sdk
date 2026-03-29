package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity

/**
 * Public contract for runtime permission checks and requests.
 */
interface AndroidPermissionSdk {
    fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus

    fun shouldShowEducation(permission: AppPermission): Boolean

    fun markEducationShown(permission: AppPermission)

    fun isRequestInProgress(permission: AppPermission): Boolean

    suspend fun request(permission: AppPermission, activity: ComponentActivity): PermissionResult
}

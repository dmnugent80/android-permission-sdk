package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.core.AppSettingsOpener
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver

/**
 * Default SDK facade implementation.
 */
class DefaultAndroidPermissionSdk internal constructor(
    private val educationStore: PermissionEducationStore,
    private val requestCoordinator: PermissionRequestCoordinator,
    private val appSettingsOpener: AppSettingsOpener,
    private val statusResolver: PermissionStatusResolver,
    private val resultResolver: PermissionResultResolver
) : AndroidPermissionSdk {

    override fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus {
        val status = statusResolver.resolve(permission, activity)
        if (
            status != PermissionStatus.PermanentlyDenied &&
            educationStore.wasPermanentlyDenied(permission)
        ) {
            educationStore.setPermanentlyDenied(permission, permanentlyDenied = false)
        }
        return status
    }

    override fun shouldShowEducation(permission: AppPermission): Boolean {
        return !educationStore.wasEducationShown(permission)
    }

    override fun markEducationShown(permission: AppPermission) {
        educationStore.markEducationShown(permission)
    }

    override suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): PermissionResult {
        val requestResult = requestCoordinator.request(permission, activity)
        if (requestResult.isEmpty()) {
            return PermissionResult.Cancelled
        }

        educationStore.markRequested(permission)
        val result = resultResolver.resolve(permission, activity, requestResult)
        when (result) {
            PermissionResult.PermanentlyDenied -> {
                educationStore.setPermanentlyDenied(permission, permanentlyDenied = true)
            }
            PermissionResult.Granted,
            PermissionResult.Denied -> {
                educationStore.setPermanentlyDenied(permission, permanentlyDenied = false)
            }
            PermissionResult.Cancelled -> Unit
        }
        return result
    }

    override fun openAppSettings(activity: Activity) {
        appSettingsOpener.open(activity)
    }
}

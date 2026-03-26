package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
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
    private val statusResolver: PermissionStatusResolver,
    private val resultResolver: PermissionResultResolver
) : AndroidPermissionSdk {

    override fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus {
        return statusResolver.resolve(permission, activity)
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
        return resultResolver.resolve(permission, activity, requestResult)
    }
}

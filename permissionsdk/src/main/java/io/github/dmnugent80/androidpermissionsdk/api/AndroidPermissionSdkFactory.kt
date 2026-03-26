package io.github.dmnugent80.androidpermissionsdk.api

import android.content.Context
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionChecker
import io.github.dmnugent80.androidpermissionsdk.platform.ActivityResultPermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.platform.SharedPrefsPermissionEducationStore

/**
 * Factory for creating a fully wired SDK instance.
 */
object AndroidPermissionSdkFactory {
    fun create(context: Context): AndroidPermissionSdk {
        val checker = AndroidPermissionChecker()
        val educationStore = SharedPrefsPermissionEducationStore(context.applicationContext)
        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            educationStore = educationStore
        )
        val resultResolver = PermissionResultResolver(
            permissionChecker = checker
        )

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = ActivityResultPermissionRequestCoordinator(),
            statusResolver = statusResolver,
            resultResolver = resultResolver
        )
    }
}

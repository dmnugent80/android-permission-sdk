package io.github.dmnugent80.androidpermissionsdk.api

import android.content.Context
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermanentDenialPolicy
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidAppSettingsOpener
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionChecker
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidRationaleChecker
import io.github.dmnugent80.androidpermissionsdk.platform.FragmentPermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.platform.SharedPrefsPermissionEducationStore

/**
 * Factory for creating a fully wired SDK instance.
 */
object AndroidPermissionSdkFactory {
    fun create(context: Context): AndroidPermissionSdk {
        val appContext = context.applicationContext
        val checker = AndroidPermissionChecker()
        val rationaleChecker = AndroidRationaleChecker()
        val educationStore = SharedPrefsPermissionEducationStore(appContext)
        val denialPolicy = PermanentDenialPolicy()
        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker,
            educationStore = educationStore,
            permanentDenialPolicy = denialPolicy
        )
        val resultResolver = PermissionResultResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker,
            educationStore = educationStore,
            permanentDenialPolicy = denialPolicy
        )

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = FragmentPermissionRequestCoordinator(),
            appSettingsOpener = AndroidAppSettingsOpener(appContext),
            statusResolver = statusResolver,
            resultResolver = resultResolver
        )
    }
}

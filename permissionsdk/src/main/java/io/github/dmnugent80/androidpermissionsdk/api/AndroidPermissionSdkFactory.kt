package io.github.dmnugent80.androidpermissionsdk.api

import android.content.Context
import io.github.dmnugent80.androidpermissionsdk.core.DiagnosticsEmitter
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import io.github.dmnugent80.androidpermissionsdk.platform.ActivityResultPermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionApiLevelChecker
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionChecker
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionManifestChecker
import io.github.dmnugent80.androidpermissionsdk.platform.AndroidPermissionRationaleChecker
import io.github.dmnugent80.androidpermissionsdk.platform.DefaultDiagnosticsEmitter
import io.github.dmnugent80.androidpermissionsdk.platform.InMemoryPermissionRequestTracker
import io.github.dmnugent80.androidpermissionsdk.platform.NoOpDiagnosticsEmitter
import io.github.dmnugent80.androidpermissionsdk.platform.SharedPrefsPermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.platform.SharedPrefsPermissionGrantHistoryStore

/**
 * Factory for creating a fully wired SDK instance.
 */
object AndroidPermissionSdkFactory {
    fun create(context: Context): AndroidPermissionSdk {
        return create(context, PermissionSdkConfig.DEFAULT)
    }

    fun create(context: Context, config: PermissionSdkConfig): AndroidPermissionSdk {
        val appContext = context.applicationContext
        val diagnostics: DiagnosticsEmitter = if (config.logger != null || config.eventListener != null) {
            DefaultDiagnosticsEmitter(config)
        } else {
            NoOpDiagnosticsEmitter
        }

        val checker = AndroidPermissionChecker()
        val educationStore = SharedPrefsPermissionEducationStore(appContext)
        val grantHistoryStore = SharedPrefsPermissionGrantHistoryStore(appContext)
        val apiLevelChecker = AndroidPermissionApiLevelChecker()
        val manifestChecker = AndroidPermissionManifestChecker(appContext)
        val rationaleChecker = AndroidPermissionRationaleChecker()
        val requestTracker = InMemoryPermissionRequestTracker()

        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            educationStore = educationStore,
            grantHistoryStore = grantHistoryStore,
            apiLevelChecker = apiLevelChecker,
            manifestChecker = manifestChecker,
            rationaleChecker = rationaleChecker,
            requestTracker = requestTracker
        )
        val resultResolver = PermissionResultResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker
        )

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = ActivityResultPermissionRequestCoordinator(diagnostics),
            statusResolver = statusResolver,
            resultResolver = resultResolver,
            apiLevelChecker = apiLevelChecker,
            manifestChecker = manifestChecker,
            requestTracker = requestTracker,
            diagnostics = diagnostics
        )
    }
}

package io.github.dmnugent80.androidpermissionsdk.platform

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.DiagnosticsEmitter
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class ActivityResultPermissionRequestCoordinator(
    private val diagnostics: DiagnosticsEmitter = NoOpDiagnosticsEmitter
) : PermissionRequestCoordinator {
    private val requestMutex = Mutex()
    private val requestId = AtomicLong(0L)

    override suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): Map<String, Boolean> {
        return requestMutex.withLock {
            withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        val requestKey = "${REQUEST_KEY_PREFIX}_${requestId.incrementAndGet()}"
                        lateinit var launcher: ActivityResultLauncher<Array<String>>
                        fun unregisterLauncher() {
                            runCatching { launcher.unregister() }
                        }
                        launcher = activity.activityResultRegistry.register(
                            requestKey,
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { result ->
                            unregisterLauncher()
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        }

                        continuation.invokeOnCancellation {
                            unregisterLauncher()
                        }

                        launcher.launch(permission.androidPermissions)
                    } catch (e: IllegalStateException) {
                        diagnostics.emitLauncherRegistrationFailed(permission, e)
                        continuation.resume(emptyMap())
                    }
                }
            }
        }
    }

    private companion object {
        const val REQUEST_KEY_PREFIX = "android_permission_sdk_request_key"
    }
}

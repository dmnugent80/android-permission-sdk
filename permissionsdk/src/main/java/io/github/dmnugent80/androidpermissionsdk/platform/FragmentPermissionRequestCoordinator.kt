package io.github.dmnugent80.androidpermissionsdk.platform

import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.internal.PermissionRequestFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FragmentPermissionRequestCoordinator : PermissionRequestCoordinator {
    private val requestMutex = Mutex()

    override suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): Map<String, Boolean> {
        if (activity !is FragmentActivity) {
            return emptyMap()
        }

        return requestMutex.withLock {
            withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine { continuation ->
                    if (activity.supportFragmentManager.isStateSaved) {
                        continuation.resume(emptyMap())
                        return@suspendCancellableCoroutine
                    }

                    val fragment = getOrCreateRequestFragment(activity)
                    fragment.requestPermissions(permission.androidPermissions) { result ->
                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }

                    continuation.invokeOnCancellation {
                        fragment.clearPendingRequest()
                    }
                }
            }
        }
    }

    private fun getOrCreateRequestFragment(activity: FragmentActivity): PermissionRequestFragment {
        val fragmentManager = activity.supportFragmentManager
        val existing =
            fragmentManager.findFragmentByTag(REQUEST_FRAGMENT_TAG) as? PermissionRequestFragment
        if (existing != null) {
            return existing
        }

        val newFragment = PermissionRequestFragment()
        fragmentManager.beginTransaction()
            .add(newFragment, REQUEST_FRAGMENT_TAG)
            .commitNow()
        return newFragment
    }

    private companion object {
        const val REQUEST_FRAGMENT_TAG = "android_permission_sdk_request_fragment"
    }
}

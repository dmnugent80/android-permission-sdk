package io.github.dmnugent80.androidpermissionsdk.platform

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestTracker
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryPermissionRequestTracker : PermissionRequestTracker {

    private val inProgress: MutableSet<AppPermission> = ConcurrentHashMap.newKeySet()

    override fun isRequestInProgress(permission: AppPermission): Boolean {
        return permission in inProgress
    }

    override fun markRequestStarted(permission: AppPermission) {
        inProgress.add(permission)
    }

    override fun markRequestCompleted(permission: AppPermission) {
        inProgress.remove(permission)
    }
}

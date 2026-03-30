package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Listener for SDK diagnostic events.
 * Implement this to receive structured lifecycle events.
 */
fun interface PermissionSdkEventListener {
    fun onEvent(event: PermissionSdkEvent)
}

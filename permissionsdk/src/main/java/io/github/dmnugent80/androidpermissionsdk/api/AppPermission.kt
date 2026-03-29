package io.github.dmnugent80.androidpermissionsdk.api

import android.Manifest

/**
 * Permissions supported by the SDK.
 */
enum class AppPermission(
    internal val storageKey: String,
    internal val androidPermissions: Array<String>,
    internal val minApiLevel: Int? = null
) {
    Camera(
        storageKey = "camera",
        androidPermissions = arrayOf(Manifest.permission.CAMERA)
    ),
    FineLocation(
        storageKey = "fine_location",
        androidPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    )
}

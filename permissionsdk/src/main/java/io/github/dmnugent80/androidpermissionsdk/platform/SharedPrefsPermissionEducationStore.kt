package io.github.dmnugent80.androidpermissionsdk.platform

import android.content.Context
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore

internal class SharedPrefsPermissionEducationStore(context: Context) : PermissionEducationStore {
    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun wasEducationShown(permission: AppPermission): Boolean {
        return sharedPreferences.getBoolean(educationKey(permission), false)
    }

    override fun markEducationShown(permission: AppPermission) {
        sharedPreferences.edit()
            .putBoolean(educationKey(permission), true)
            .apply()
    }

    override fun wasRequested(permission: AppPermission): Boolean {
        return sharedPreferences.getBoolean(requestedKey(permission), false)
    }

    override fun markRequested(permission: AppPermission) {
        sharedPreferences.edit()
            .putBoolean(requestedKey(permission), true)
            .apply()
    }

    override fun wasPermanentlyDenied(permission: AppPermission): Boolean {
        return sharedPreferences.getBoolean(permanentDeniedKey(permission), false)
    }

    override fun setPermanentlyDenied(permission: AppPermission, permanentlyDenied: Boolean) {
        sharedPreferences.edit()
            .putBoolean(permanentDeniedKey(permission), permanentlyDenied)
            .apply()
    }

    private fun educationKey(permission: AppPermission): String {
        return "education_shown_${permission.storageKey}"
    }

    private fun requestedKey(permission: AppPermission): String {
        return "request_attempted_${permission.storageKey}"
    }

    private fun permanentDeniedKey(permission: AppPermission): String {
        return "permanent_denial_${permission.storageKey}"
    }

    internal companion object {
        const val PREFS_NAME = "android_permission_sdk_flags"
    }
}

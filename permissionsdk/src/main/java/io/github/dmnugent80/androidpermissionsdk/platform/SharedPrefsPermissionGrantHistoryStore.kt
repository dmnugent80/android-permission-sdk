package io.github.dmnugent80.androidpermissionsdk.platform

import android.content.Context
import androidx.core.content.edit
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionGrantHistoryStore

internal class SharedPrefsPermissionGrantHistoryStore(context: Context) : PermissionGrantHistoryStore {
    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun wasEverGranted(permission: AppPermission): Boolean {
        return sharedPreferences.getBoolean(grantHistoryKey(permission), false)
    }

    override fun markGranted(permission: AppPermission) {
        sharedPreferences.edit {
            putBoolean(grantHistoryKey(permission), true)
        }
    }

    private fun grantHistoryKey(permission: AppPermission): String {
        return "grant_history_${permission.storageKey}"
    }

    internal companion object {
        const val PREFS_NAME = "android_permission_sdk_grant_history"
    }
}

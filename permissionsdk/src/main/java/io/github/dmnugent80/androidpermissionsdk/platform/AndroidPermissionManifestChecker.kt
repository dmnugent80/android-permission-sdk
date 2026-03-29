package io.github.dmnugent80.androidpermissionsdk.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.core.PermissionManifestChecker

internal class AndroidPermissionManifestChecker(
    private val context: Context
) : PermissionManifestChecker {

    override fun isDeclaredInManifest(permission: AppPermission): Boolean {
        val declaredPermissions = getDeclaredPermissions()
        return permission.androidPermissions.all { it in declaredPermissions }
    }

    private fun getDeclaredPermissions(): Set<String> {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
        }
        return packageInfo.requestedPermissions?.toSet() ?: emptySet()
    }
}

package io.github.dmnugent80.androidpermissionsdk.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import io.github.dmnugent80.androidpermissionsdk.core.AppSettingsOpener

internal class AndroidAppSettingsOpener(
    private val appContext: Context
) : AppSettingsOpener {

    override fun open(activity: Activity) {
        activity.startActivity(createIntent())
    }

    internal fun createIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", appContext.packageName, null)
        }
    }
}

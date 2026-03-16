package io.github.dmnugent80.androidpermissionsdk.platform

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidAppSettingsOpenerInstrumentedTest {
    @Test
    fun createIntent_usesAppPackageUri() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val opener = AndroidAppSettingsOpener(context)

        val intent = opener.createIntent()

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${context.packageName}", intent.dataString)
    }
}

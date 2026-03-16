package io.github.dmnugent80.androidpermissionsdk.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidPermissionSdkFactoryInstrumentedTest {
    @Test
    fun create_returnsSdkInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val sdk = AndroidPermissionSdkFactory.create(context)

        assertNotNull(sdk)
    }
}

package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.core.AppSettingsOpener
import io.github.dmnugent80.androidpermissionsdk.core.PermissionChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermanentDenialPolicy
import io.github.dmnugent80.androidpermissionsdk.core.RationaleChecker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class DefaultAndroidPermissionSdkTest {
    private val activity: Activity = mock(Activity::class.java)
    private val componentActivity: ComponentActivity = mock(ComponentActivity::class.java)

    @Test
    fun `request marks permanent denied when request result resolves to permanently denied`() = runBlocking {
        val educationStore = FakeEducationStore()
        val checker = FakePermissionChecker(granted = false)
        val rationaleChecker = FakeRationaleChecker(shouldShow = false)
        val sdk = createSdk(
            educationStore = educationStore,
            checker = checker,
            rationaleChecker = rationaleChecker,
            requestResult = mapOf(android.Manifest.permission.CAMERA to false)
        )

        sdk.request(AppPermission.Camera, componentActivity)

        assertTrue(educationStore.wasPermanentlyDenied(AppPermission.Camera))
    }

    @Test
    fun `request clears permanent denied when request result resolves to granted`() = runBlocking {
        val educationStore = FakeEducationStore()
        educationStore.setPermanentlyDenied(AppPermission.Camera, true)
        val checker = FakePermissionChecker(granted = true)
        val rationaleChecker = FakeRationaleChecker(shouldShow = false)
        val sdk = createSdk(
            educationStore = educationStore,
            checker = checker,
            rationaleChecker = rationaleChecker,
            requestResult = mapOf(android.Manifest.permission.CAMERA to true)
        )

        sdk.request(AppPermission.Camera, componentActivity)

        assertFalse(educationStore.wasPermanentlyDenied(AppPermission.Camera))
    }

    @Test
    fun `getStatus clears stale permanent denied marker when status is not permanently denied`() {
        val educationStore = FakeEducationStore()
        educationStore.markRequested(AppPermission.Camera)
        educationStore.setPermanentlyDenied(AppPermission.Camera, true)
        val checker = FakePermissionChecker(granted = false)
        val rationaleChecker = FakeRationaleChecker(shouldShow = true)
        val sdk = createSdk(
            educationStore = educationStore,
            checker = checker,
            rationaleChecker = rationaleChecker
        )

        sdk.getStatus(AppPermission.Camera, activity)

        assertFalse(educationStore.wasPermanentlyDenied(AppPermission.Camera))
    }

    private fun createSdk(
        educationStore: FakeEducationStore,
        checker: FakePermissionChecker,
        rationaleChecker: FakeRationaleChecker,
        requestResult: Map<String, Boolean> = emptyMap()
    ): DefaultAndroidPermissionSdk {
        val policy = PermanentDenialPolicy()
        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker,
            educationStore = educationStore,
            permanentDenialPolicy = policy
        )
        val resultResolver = PermissionResultResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker,
            educationStore = educationStore,
            permanentDenialPolicy = policy
        )

        val requestCoordinator = FakeRequestCoordinator(requestResult)
        val appSettingsOpener = FakeAppSettingsOpener()

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = requestCoordinator,
            appSettingsOpener = appSettingsOpener,
            statusResolver = statusResolver,
            resultResolver = resultResolver
        )
    }

    private class FakeEducationStore : PermissionEducationStore {
        private val educationShown = mutableMapOf<AppPermission, Boolean>()
        private val requested = mutableMapOf<AppPermission, Boolean>()
        private val permanentlyDenied = mutableMapOf<AppPermission, Boolean>()

        override fun wasEducationShown(permission: AppPermission): Boolean {
            return educationShown[permission] ?: false
        }

        override fun markEducationShown(permission: AppPermission) {
            educationShown[permission] = true
        }

        override fun wasRequested(permission: AppPermission): Boolean {
            return requested[permission] ?: false
        }

        override fun markRequested(permission: AppPermission) {
            requested[permission] = true
        }

        override fun wasPermanentlyDenied(permission: AppPermission): Boolean {
            return permanentlyDenied[permission] ?: false
        }

        override fun setPermanentlyDenied(permission: AppPermission, permanentlyDenied: Boolean) {
            this.permanentlyDenied[permission] = permanentlyDenied
        }
    }

    private class FakeRequestCoordinator(
        private val requestResult: Map<String, Boolean>
    ) : PermissionRequestCoordinator {
        override suspend fun request(
            permission: AppPermission,
            activity: ComponentActivity
        ): Map<String, Boolean> {
            return requestResult
        }
    }

    private class FakeAppSettingsOpener : AppSettingsOpener {
        override fun open(activity: Activity) = Unit
    }

    private class FakePermissionChecker(
        private val granted: Boolean
    ) : PermissionChecker {
        override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
            return granted
        }
    }

    private class FakeRationaleChecker(
        private val shouldShow: Boolean
    ) : RationaleChecker {
        override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
            return shouldShow
        }
    }
}

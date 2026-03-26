package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.core.PermissionChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.core.PermissionResultResolver
import io.github.dmnugent80.androidpermissionsdk.core.PermissionStatusResolver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class DefaultAndroidPermissionSdkTest {
    private val activity: Activity = mock(Activity::class.java)
    private val componentActivity: ComponentActivity = mock(ComponentActivity::class.java)

    @Test
    fun `request returns cancelled and does not mark requested when request map is empty`() =
        runBlocking {
            val educationStore = FakeEducationStore()
            val sdk = createSdk(
                educationStore = educationStore,
                granted = false,
                requestResult = emptyMap()
            )

            val result = sdk.request(AppPermission.Camera, componentActivity)

            assertEquals(PermissionResult.Cancelled, result)
            assertFalse(educationStore.wasRequested(AppPermission.Camera))
        }

    @Test
    fun `request returns denied and marks requested when permission is not granted`() = runBlocking {
        val educationStore = FakeEducationStore()
        val sdk = createSdk(
            educationStore = educationStore,
            granted = false,
            requestResult = mapOf(android.Manifest.permission.CAMERA to false)
        )

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.Denied, result)
        assertTrue(educationStore.wasRequested(AppPermission.Camera))
    }

    @Test
    fun `request returns granted and marks requested when permission is granted`() = runBlocking {
        val educationStore = FakeEducationStore()
        val sdk = createSdk(
            educationStore = educationStore,
            granted = true,
            requestResult = mapOf(android.Manifest.permission.CAMERA to true)
        )

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.Granted, result)
        assertTrue(educationStore.wasRequested(AppPermission.Camera))
    }

    @Test
    fun `getStatus returns denied when request history exists and permission is not granted`() {
        val educationStore = FakeEducationStore().apply {
            markRequested(AppPermission.Camera)
        }
        val sdk = createSdk(
            educationStore = educationStore,
            granted = false,
            requestResult = emptyMap()
        )

        val status = sdk.getStatus(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied, status)
    }

    private fun createSdk(
        educationStore: FakeEducationStore,
        granted: Boolean,
        requestResult: Map<String, Boolean>
    ): DefaultAndroidPermissionSdk {
        val checker = FakePermissionChecker(granted = granted)
        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            educationStore = educationStore
        )
        val resultResolver = PermissionResultResolver(permissionChecker = checker)

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = FakeRequestCoordinator(requestResult),
            statusResolver = statusResolver,
            resultResolver = resultResolver
        )
    }

    private class FakeEducationStore : PermissionEducationStore {
        private val educationShown = mutableMapOf<AppPermission, Boolean>()
        private val requested = mutableMapOf<AppPermission, Boolean>()

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

    private class FakePermissionChecker(
        private val granted: Boolean
    ) : PermissionChecker {
        override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
            return granted
        }
    }
}

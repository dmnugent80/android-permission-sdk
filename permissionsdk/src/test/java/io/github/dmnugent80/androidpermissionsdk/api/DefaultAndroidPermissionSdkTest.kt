package io.github.dmnugent80.androidpermissionsdk.api

import android.app.Activity
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.core.PermissionApiLevelChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionEducationStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionGrantHistoryStore
import io.github.dmnugent80.androidpermissionsdk.core.PermissionManifestChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRationaleChecker
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestCoordinator
import io.github.dmnugent80.androidpermissionsdk.core.PermissionRequestTracker
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
    fun `request returns unavailable on api level when api level check fails`() = runBlocking {
        val sdk = createSdk(apiLevelAvailable = false)

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.UnavailableOnApiLevel, result)
    }

    @Test
    fun `request returns missing from manifest when not declared`() = runBlocking {
        val sdk = createSdk(manifestDeclared = false)

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.MissingFromManifest, result)
    }

    @Test
    fun `request returns already in progress when request is ongoing`() = runBlocking {
        val requestTracker = FakeRequestTracker().apply { inProgress = true }
        val sdk = createSdk(requestTracker = requestTracker)

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.AlreadyInProgress, result)
    }

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
            requestResult = mapOf(android.Manifest.permission.CAMERA to false),
            shouldShowRationale = false
        )

        val result = sdk.request(AppPermission.Camera, componentActivity)

        assertEquals(PermissionResult.Denied(canRequestAgain = false), result)
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
            requestResult = emptyMap(),
            shouldShowRationale = true
        )

        val status = sdk.getStatus(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied(canRequestAgain = true), status)
    }

    @Test
    fun `request clears in progress flag after completion`() = runBlocking {
        val requestTracker = FakeRequestTracker()
        val sdk = createSdk(
            requestTracker = requestTracker,
            granted = true,
            requestResult = mapOf(android.Manifest.permission.CAMERA to true)
        )

        sdk.request(AppPermission.Camera, componentActivity)

        assertFalse(requestTracker.isRequestInProgress(AppPermission.Camera))
    }

    private fun createSdk(
        educationStore: FakeEducationStore = FakeEducationStore(),
        granted: Boolean = true,
        requestResult: Map<String, Boolean> = emptyMap(),
        apiLevelAvailable: Boolean = true,
        manifestDeclared: Boolean = true,
        shouldShowRationale: Boolean = false,
        requestTracker: FakeRequestTracker = FakeRequestTracker()
    ): DefaultAndroidPermissionSdk {
        val checker = FakePermissionChecker(granted = granted)
        val apiLevelChecker = FakeApiLevelChecker(apiLevelAvailable)
        val manifestChecker = FakeManifestChecker(manifestDeclared)
        val rationaleChecker = FakeRationaleChecker(shouldShowRationale)

        val statusResolver = PermissionStatusResolver(
            permissionChecker = checker,
            educationStore = educationStore,
            grantHistoryStore = FakeGrantHistoryStore(),
            apiLevelChecker = apiLevelChecker,
            manifestChecker = manifestChecker,
            rationaleChecker = rationaleChecker,
            requestTracker = requestTracker
        )
        val resultResolver = PermissionResultResolver(
            permissionChecker = checker,
            rationaleChecker = rationaleChecker
        )

        return DefaultAndroidPermissionSdk(
            educationStore = educationStore,
            requestCoordinator = FakeRequestCoordinator(requestResult),
            statusResolver = statusResolver,
            resultResolver = resultResolver,
            apiLevelChecker = apiLevelChecker,
            manifestChecker = manifestChecker,
            requestTracker = requestTracker
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

    private class FakeApiLevelChecker(
        private val available: Boolean = true
    ) : PermissionApiLevelChecker {
        override fun isAvailableOnCurrentApiLevel(permission: AppPermission): Boolean = available
    }

    private class FakeManifestChecker(
        private val declared: Boolean = true
    ) : PermissionManifestChecker {
        override fun isDeclaredInManifest(permission: AppPermission): Boolean = declared
    }

    private class FakeRationaleChecker(
        private val shouldShow: Boolean = false
    ) : PermissionRationaleChecker {
        override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
            return shouldShow
        }
    }

    class FakeRequestTracker : PermissionRequestTracker {
        var inProgress: Boolean = false

        override fun isRequestInProgress(permission: AppPermission): Boolean = inProgress
        override fun markRequestStarted(permission: AppPermission) { inProgress = true }
        override fun markRequestCompleted(permission: AppPermission) { inProgress = false }
    }

    private class FakeGrantHistoryStore : PermissionGrantHistoryStore {
        override fun wasEverGranted(permission: AppPermission): Boolean = false
        override fun markGranted(permission: AppPermission) = Unit
    }
}

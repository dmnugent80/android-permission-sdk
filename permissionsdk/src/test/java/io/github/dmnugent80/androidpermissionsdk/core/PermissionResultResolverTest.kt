package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class PermissionResultResolverTest {
    private val activity: Activity = mock(Activity::class.java)
    private val checker = FakePermissionChecker()
    private val rationaleChecker = FakeRationaleChecker()
    private val educationStore = FakeEducationStore()
    private val resolver = PermissionResultResolver(
        permissionChecker = checker,
        rationaleChecker = rationaleChecker,
        educationStore = educationStore,
        permanentDenialPolicy = PermanentDenialPolicy()
    )

    @Test
    fun `returns cancelled when request result map is empty`() {
        val result = resolver.resolve(AppPermission.Camera, activity, emptyMap())

        assertEquals(PermissionResult.Cancelled, result)
    }

    @Test
    fun `returns granted when permission checker reports granted after request`() {
        checker.granted = true

        val result = resolver.resolve(
            AppPermission.Camera,
            activity,
            mapOf(android.Manifest.permission.CAMERA to true)
        )

        assertEquals(PermissionResult.Granted, result)
    }

    @Test
    fun `returns denied when not granted and rationale is true`() {
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = true

        val result = resolver.resolve(
            AppPermission.Camera,
            activity,
            mapOf(android.Manifest.permission.CAMERA to false)
        )

        assertEquals(PermissionResult.Denied, result)
    }

    @Test
    fun `returns permanently denied when not granted and rationale is false with history`() {
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = false

        val result = resolver.resolve(
            AppPermission.Camera,
            activity,
            mapOf(android.Manifest.permission.CAMERA to false)
        )

        assertEquals(PermissionResult.PermanentlyDenied, result)
    }

    private class FakePermissionChecker : PermissionChecker {
        var granted: Boolean = false

        override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
            return granted
        }
    }

    private class FakeRationaleChecker : RationaleChecker {
        var shouldShow: Boolean = false

        override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
            return shouldShow
        }
    }

    private class FakeEducationStore : PermissionEducationStore {
        var educationShown: Boolean = false
        var requested: Boolean = false

        override fun wasEducationShown(permission: AppPermission): Boolean = educationShown

        override fun markEducationShown(permission: AppPermission) {
            educationShown = true
        }

        override fun wasRequested(permission: AppPermission): Boolean = requested

        override fun markRequested(permission: AppPermission) {
            requested = true
        }
    }
}

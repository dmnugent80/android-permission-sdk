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
    private val resolver = PermissionResultResolver(
        permissionChecker = checker,
        rationaleChecker = rationaleChecker
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
    fun `returns denied with canRequestAgain true when rationale should be shown`() {
        checker.granted = false
        rationaleChecker.shouldShow = true

        val result = resolver.resolve(
            AppPermission.Camera,
            activity,
            mapOf(android.Manifest.permission.CAMERA to false)
        )

        assertEquals(PermissionResult.Denied(canRequestAgain = true), result)
    }

    @Test
    fun `returns denied with canRequestAgain false when rationale should not be shown`() {
        checker.granted = false
        rationaleChecker.shouldShow = false

        val result = resolver.resolve(
            AppPermission.Camera,
            activity,
            mapOf(android.Manifest.permission.CAMERA to false)
        )

        assertEquals(PermissionResult.Denied(canRequestAgain = false), result)
    }

    private class FakePermissionChecker : PermissionChecker {
        var granted: Boolean = false

        override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
            return granted
        }
    }

    private class FakeRationaleChecker : PermissionRationaleChecker {
        var shouldShow: Boolean = false

        override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
            return shouldShow
        }
    }
}

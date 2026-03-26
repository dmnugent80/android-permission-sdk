package io.github.dmnugent80.androidpermissionsdk.core

import android.app.Activity
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class PermissionStatusResolverTest {
    private val activity: Activity = mock(Activity::class.java)
    private val checker = FakePermissionChecker()
    private val rationaleChecker = FakeRationaleChecker()
    private val educationStore = FakeEducationStore()
    private val resolver = PermissionStatusResolver(
        permissionChecker = checker,
        rationaleChecker = rationaleChecker,
        educationStore = educationStore,
        permanentDenialPolicy = PermanentDenialPolicy()
    )

    @Test
    fun `returns granted when permission checker reports granted`() {
        checker.granted = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Granted, status)
    }

    @Test
    fun `returns not requested yet when denied and no history`() {
        checker.granted = false
        educationStore.educationShown = false
        educationStore.requested = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.NotRequestedYet, status)
    }

    @Test
    fun `returns not requested yet when only education history exists`() {
        checker.granted = false
        educationStore.educationShown = true
        educationStore.requested = false
        rationaleChecker.shouldShow = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.NotRequestedYet, status)
    }

    @Test
    fun `returns denied when there is history and rationale is true`() {
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied, status)
    }

    @Test
    fun `returns denied when request history exists and rationale is false but not marked permanent`() {
        checker.granted = false
        educationStore.requested = true
        educationStore.permanentlyDenied = false
        rationaleChecker.shouldShow = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied, status)
    }

    @Test
    fun `returns permanently denied when there is history and rationale is false`() {
        checker.granted = false
        educationStore.requested = true
        educationStore.permanentlyDenied = true
        rationaleChecker.shouldShow = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.PermanentlyDenied, status)
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
        var permanentlyDenied: Boolean = false

        override fun wasEducationShown(permission: AppPermission): Boolean = educationShown

        override fun markEducationShown(permission: AppPermission) {
            educationShown = true
        }

        override fun wasRequested(permission: AppPermission): Boolean = requested

        override fun markRequested(permission: AppPermission) {
            requested = true
        }

        override fun wasPermanentlyDenied(permission: AppPermission): Boolean = permanentlyDenied

        override fun setPermanentlyDenied(permission: AppPermission, permanentlyDenied: Boolean) {
            this.permanentlyDenied = permanentlyDenied
        }
    }
}

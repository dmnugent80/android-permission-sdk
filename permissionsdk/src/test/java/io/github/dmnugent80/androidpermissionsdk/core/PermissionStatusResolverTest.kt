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
    private val educationStore = FakeEducationStore()
    private val grantHistoryStore = FakeGrantHistoryStore()
    private val apiLevelChecker = FakeApiLevelChecker()
    private val manifestChecker = FakeManifestChecker()
    private val rationaleChecker = FakeRationaleChecker()
    private val requestTracker = FakeRequestTracker()
    private val resolver = PermissionStatusResolver(
        permissionChecker = checker,
        educationStore = educationStore,
        grantHistoryStore = grantHistoryStore,
        apiLevelChecker = apiLevelChecker,
        manifestChecker = manifestChecker,
        rationaleChecker = rationaleChecker,
        requestTracker = requestTracker
    )

    @Test
    fun `returns unavailable on api level when api level check fails`() {
        apiLevelChecker.available = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.UnavailableOnApiLevel, status)
    }

    @Test
    fun `returns missing from manifest when not declared`() {
        apiLevelChecker.available = true
        manifestChecker.declared = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.MissingFromManifest, status)
    }

    @Test
    fun `returns request in progress when request is ongoing`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.RequestInProgress, status)
    }

    @Test
    fun `returns granted when permission checker reports granted`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Granted, status)
    }

    @Test
    fun `returns not requested yet when denied and no history`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = false
        educationStore.educationShown = false
        educationStore.requested = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.NotRequestedYet, status)
    }

    @Test
    fun `returns not requested yet when only education history exists`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = false
        educationStore.educationShown = true
        educationStore.requested = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.NotRequestedYet, status)
    }

    @Test
    fun `returns denied with canRequestAgain true when rationale should be shown`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied(canRequestAgain = true), status)
    }

    @Test
    fun `returns denied with canRequestAgain false when rationale should not be shown and never granted`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = false
        grantHistoryStore.everGranted = false

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied(canRequestAgain = false), status)
    }

    @Test
    fun `returns denied with canRequestAgain true when previously granted even if rationale is false`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = false
        educationStore.requested = true
        rationaleChecker.shouldShow = false
        grantHistoryStore.everGranted = true

        val status = resolver.resolve(AppPermission.Camera, activity)

        assertEquals(PermissionStatus.Denied(canRequestAgain = true), status)
    }

    @Test
    fun `marks grant history when permission is granted`() {
        apiLevelChecker.available = true
        manifestChecker.declared = true
        requestTracker.inProgress = false
        checker.granted = true
        grantHistoryStore.everGranted = false

        resolver.resolve(AppPermission.Camera, activity)

        assertEquals(true, grantHistoryStore.everGranted)
    }

    private class FakePermissionChecker : PermissionChecker {
        var granted: Boolean = false

        override fun isGranted(activity: Activity, permission: AppPermission): Boolean {
            return granted
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

    private class FakeApiLevelChecker : PermissionApiLevelChecker {
        var available: Boolean = true

        override fun isAvailableOnCurrentApiLevel(permission: AppPermission): Boolean = available
    }

    private class FakeManifestChecker : PermissionManifestChecker {
        var declared: Boolean = true

        override fun isDeclaredInManifest(permission: AppPermission): Boolean = declared
    }

    private class FakeRationaleChecker : PermissionRationaleChecker {
        var shouldShow: Boolean = false

        override fun shouldShowRationale(activity: Activity, permission: AppPermission): Boolean {
            return shouldShow
        }
    }

    private class FakeRequestTracker : PermissionRequestTracker {
        var inProgress: Boolean = false

        override fun isRequestInProgress(permission: AppPermission): Boolean = inProgress
        override fun markRequestStarted(permission: AppPermission) { inProgress = true }
        override fun markRequestCompleted(permission: AppPermission) { inProgress = false }
    }

    private class FakeGrantHistoryStore : PermissionGrantHistoryStore {
        var everGranted: Boolean = false

        override fun wasEverGranted(permission: AppPermission): Boolean = everGranted
        override fun markGranted(permission: AppPermission) { everGranted = true }
    }
}

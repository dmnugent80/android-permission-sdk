package io.github.dmnugent80.androidpermissionsdk.sample

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import io.github.dmnugent80.androidpermissionsdk.api.AndroidPermissionSdk
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionSampleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var sdk: FakeAndroidPermissionSdk
    private lateinit var viewModel: PermissionSampleViewModel

    private val activity = Mockito.mock(Activity::class.java)
    private val componentActivity = Mockito.mock(ComponentActivity::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sdk = FakeAndroidPermissionSdk()
        viewModel = PermissionSampleViewModel(sdk)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshAll_populatesPermissionSections() {
        sdk.cameraStatus = PermissionStatus.Granted
        sdk.locationStatus = PermissionStatus.Denied(canRequestAgain = true)
        sdk.cameraEducation = false
        sdk.locationEducation = true

        viewModel.refreshAll(activity)

        val state = viewModel.uiState.value
        assertEquals(PermissionStatus.Granted, state.camera.status)
        assertEquals(PermissionStatus.Denied(canRequestAgain = true), state.location.status)
        assertFalse(state.camera.shouldShowEducation)
        assertEquals(true, state.location.shouldShowEducation)
    }

    @Test
    fun requestFlows_updateLastResultAndRefreshStatus() = runTest {
        sdk.nextCameraResult = PermissionResult.Granted
        sdk.nextLocationResult = PermissionResult.Denied(canRequestAgain = false)

        viewModel.requestCamera(componentActivity)
        viewModel.requestLocation(componentActivity)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PermissionResult.Granted, state.camera.lastResult)
        assertEquals(PermissionResult.Denied(canRequestAgain = false), state.location.lastResult)
        assertEquals(PermissionStatus.Granted, state.camera.status)
        assertEquals(PermissionStatus.Denied(canRequestAgain = false), state.location.status)
    }

    @Test
    fun markingEducationShown_updatesFlags() {
        sdk.cameraEducation = true
        sdk.locationEducation = true
        viewModel.refreshAll(activity)

        viewModel.markCameraEducationShown()
        viewModel.markLocationEducationShown()

        val state = viewModel.uiState.value
        assertFalse(state.camera.shouldShowEducation)
        assertFalse(state.location.shouldShowEducation)
    }

    @Test
    fun clearDebugState_clearsLastResultsAndRefreshesFromSdk() = runTest {
        sdk.nextCameraResult = PermissionResult.Granted
        sdk.nextLocationResult = PermissionResult.Denied(canRequestAgain = false)
        viewModel.requestCamera(componentActivity)
        viewModel.requestLocation(componentActivity)
        advanceUntilIdle()

        sdk.cameraStatus = PermissionStatus.Denied(canRequestAgain = true)
        sdk.locationStatus = PermissionStatus.Denied(canRequestAgain = false)

        val context = Mockito.mock(Context::class.java)
        val sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        val editor = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(
            context.getSharedPreferences(SDK_DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
        ).thenReturn(sharedPreferences)
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.clear()).thenReturn(editor)

        viewModel.clearDebugState(context = context, activity = activity)

        val state = viewModel.uiState.value
        assertNull(state.camera.lastResult)
        assertNull(state.location.lastResult)
        assertEquals(PermissionStatus.Denied(canRequestAgain = true), state.camera.status)
        assertEquals(PermissionStatus.Denied(canRequestAgain = false), state.location.status)
        Mockito.verify(editor).clear()
        Mockito.verify(editor).apply()
    }
}

private class FakeAndroidPermissionSdk : AndroidPermissionSdk {
    var cameraStatus: PermissionStatus = PermissionStatus.NotRequestedYet
    var locationStatus: PermissionStatus = PermissionStatus.NotRequestedYet
    var cameraEducation: Boolean = true
    var locationEducation: Boolean = true
    var nextCameraResult: PermissionResult = PermissionResult.Denied(canRequestAgain = false)
    var nextLocationResult: PermissionResult = PermissionResult.Denied(canRequestAgain = false)

    override fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus {
        return when (permission) {
            AppPermission.Camera -> cameraStatus
            AppPermission.FineLocation -> locationStatus
        }
    }

    override fun shouldShowEducation(permission: AppPermission): Boolean {
        return when (permission) {
            AppPermission.Camera -> cameraEducation
            AppPermission.FineLocation -> locationEducation
        }
    }

    override fun markEducationShown(permission: AppPermission) {
        when (permission) {
            AppPermission.Camera -> cameraEducation = false
            AppPermission.FineLocation -> locationEducation = false
        }
    }

    override fun isRequestInProgress(permission: AppPermission): Boolean = false

    override suspend fun request(
        permission: AppPermission,
        activity: ComponentActivity
    ): PermissionResult {
        return when (permission) {
            AppPermission.Camera -> {
                nextCameraResult.also { cameraStatus = it.toStatus() }
            }
            AppPermission.FineLocation -> {
                nextLocationResult.also { locationStatus = it.toStatus() }
            }
        }
    }

    private fun PermissionResult.toStatus(): PermissionStatus {
        return when (this) {
            PermissionResult.Granted -> PermissionStatus.Granted
            is PermissionResult.Denied -> PermissionStatus.Denied(canRequestAgain)
            PermissionResult.Cancelled -> PermissionStatus.NotRequestedYet
            PermissionResult.AlreadyInProgress -> PermissionStatus.RequestInProgress
            PermissionResult.MissingFromManifest -> PermissionStatus.MissingFromManifest
            PermissionResult.UnavailableOnApiLevel -> PermissionStatus.UnavailableOnApiLevel
        }
    }
}

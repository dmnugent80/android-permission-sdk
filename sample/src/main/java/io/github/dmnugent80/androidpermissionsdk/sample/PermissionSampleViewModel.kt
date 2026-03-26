package io.github.dmnugent80.androidpermissionsdk.sample

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.dmnugent80.androidpermissionsdk.api.AndroidPermissionSdk
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PermissionSampleUiState(
    val camera: PermissionSectionUiState = PermissionSectionUiState(),
    val location: PermissionSectionUiState = PermissionSectionUiState()
)

data class PermissionSectionUiState(
    val status: PermissionStatus = PermissionStatus.NotRequestedYet,
    val shouldShowEducation: Boolean = true,
    val lastResult: PermissionResult? = null
)

class PermissionSampleViewModel(
    private val sdk: AndroidPermissionSdk
) : ViewModel() {
    private val _uiState = MutableStateFlow(PermissionSampleUiState())
    val uiState: StateFlow<PermissionSampleUiState> = _uiState.asStateFlow()

    fun refreshAll(activity: Activity) {
        val cameraStatus = sdk.getStatus(AppPermission.Camera, activity)
        val locationStatus = sdk.getStatus(AppPermission.FineLocation, activity)
        val cameraEducation = sdk.shouldShowEducation(AppPermission.Camera)
        val locationEducation = sdk.shouldShowEducation(AppPermission.FineLocation)

        _uiState.update { current ->
            current.copy(
                camera = current.camera.copy(
                    status = cameraStatus,
                    shouldShowEducation = cameraEducation
                ),
                location = current.location.copy(
                    status = locationStatus,
                    shouldShowEducation = locationEducation
                )
            )
        }
    }

    fun requestCamera(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = sdk.request(AppPermission.Camera, activity)
            _uiState.update { current ->
                current.copy(camera = current.camera.copy(lastResult = result))
            }
            refreshAll(activity)
        }
    }

    fun requestLocation(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = sdk.request(AppPermission.FineLocation, activity)
            _uiState.update { current ->
                current.copy(location = current.location.copy(lastResult = result))
            }
            refreshAll(activity)
        }
    }

    fun markCameraEducationShown() {
        sdk.markEducationShown(AppPermission.Camera)
        _uiState.update { current ->
            current.copy(
                camera = current.camera.copy(
                    shouldShowEducation = sdk.shouldShowEducation(AppPermission.Camera)
                )
            )
        }
    }

    fun markLocationEducationShown() {
        sdk.markEducationShown(AppPermission.FineLocation)
        _uiState.update { current ->
            current.copy(
                location = current.location.copy(
                    shouldShowEducation = sdk.shouldShowEducation(AppPermission.FineLocation)
                )
            )
        }
    }

    fun openSettings(activity: Activity) {
        sdk.openAppSettings(activity)
    }

    fun clearDebugState(context: Context, activity: Activity) {
        context.getSharedPreferences(SDK_DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        _uiState.update { current ->
            current.copy(
                camera = current.camera.copy(lastResult = null),
                location = current.location.copy(lastResult = null)
            )
        }

        refreshAll(activity)
    }
}

class PermissionSampleViewModelFactory(
    private val sdk: AndroidPermissionSdk
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionSampleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionSampleViewModel(sdk) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

internal const val SDK_DEBUG_PREFS_NAME = "android_permission_sdk_flags"

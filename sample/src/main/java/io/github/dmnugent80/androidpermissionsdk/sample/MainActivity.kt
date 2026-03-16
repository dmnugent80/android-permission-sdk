package io.github.dmnugent80.androidpermissionsdk.sample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.dmnugent80.androidpermissionsdk.api.AndroidPermissionSdk
import io.github.dmnugent80.androidpermissionsdk.api.AndroidPermissionSdkFactory
import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val sdk by lazy { AndroidPermissionSdkFactory.create(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    PermissionSampleScreen(
                        sdk = sdk,
                        activity = this,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionSampleScreen(
    sdk: AndroidPermissionSdk,
    activity: ComponentActivity,
    modifier: Modifier = Modifier
) {
    var cameraStatus by remember {
        mutableStateOf<PermissionStatus>(PermissionStatus.NotRequestedYet)
    }
    var locationStatus by remember {
        mutableStateOf<PermissionStatus>(PermissionStatus.NotRequestedYet)
    }
    var cameraEducation by remember { mutableStateOf(true) }
    var locationEducation by remember { mutableStateOf(true) }
    var cameraLastResult by remember { mutableStateOf<PermissionResult?>(null) }
    var locationLastResult by remember { mutableStateOf<PermissionResult?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    fun refreshAll() {
        cameraStatus = sdk.getStatus(AppPermission.Camera, activity)
        locationStatus = sdk.getStatus(AppPermission.FineLocation, activity)
        cameraEducation = sdk.shouldShowEducation(AppPermission.Camera)
        locationEducation = sdk.shouldShowEducation(AppPermission.FineLocation)
    }

    DisposableEffect(lifecycleOwner) {
        refreshAll()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Android Permission SDK Sample",
            style = MaterialTheme.typography.headlineSmall
        )

        PermissionSection(
            title = "Camera",
            status = cameraStatus,
            shouldShowEducation = cameraEducation,
            lastResult = cameraLastResult,
            onRefresh = { refreshAll() },
            onMarkEducationShown = {
                sdk.markEducationShown(AppPermission.Camera)
                refreshAll()
            },
            onRequest = {
                coroutineScope.launch {
                    cameraLastResult = sdk.request(AppPermission.Camera, activity)
                    refreshAll()
                }
            },
            onOpenSettings = { sdk.openAppSettings(activity) }
        )

        PermissionSection(
            title = "Fine Location",
            status = locationStatus,
            shouldShowEducation = locationEducation,
            lastResult = locationLastResult,
            onRefresh = { refreshAll() },
            onMarkEducationShown = {
                sdk.markEducationShown(AppPermission.FineLocation)
                refreshAll()
            },
            onRequest = {
                coroutineScope.launch {
                    locationLastResult = sdk.request(AppPermission.FineLocation, activity)
                    refreshAll()
                }
            },
            onOpenSettings = { sdk.openAppSettings(activity) }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                activity
                    .getSharedPreferences(SDK_DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                cameraLastResult = null
                locationLastResult = null
                refreshAll()
            }
        ) {
            Text("Clear debug state")
        }
    }
}

@Composable
private fun PermissionSection(
    title: String,
    status: PermissionStatus,
    shouldShowEducation: Boolean,
    lastResult: PermissionResult?,
    onRefresh: () -> Unit,
    onMarkEducationShown: () -> Unit,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Status: ${status.toDisplayText()}")
            Text(text = "Should show education: $shouldShowEducation")
            Text(text = "Last result: ${lastResult?.toDisplayText() ?: "None"}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
                Button(onClick = onRequest) {
                    Text("Request")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMarkEducationShown) {
                    Text("Mark education shown")
                }
                Button(onClick = onOpenSettings) {
                    Text("Open settings")
                }
            }
        }
    }
}

private fun PermissionStatus.toDisplayText(): String {
    return when (this) {
        PermissionStatus.Granted -> "Granted"
        PermissionStatus.NotRequestedYet -> "Not requested yet"
        PermissionStatus.Denied -> "Denied"
        PermissionStatus.PermanentlyDenied -> "Permanently denied"
    }
}

private fun PermissionResult.toDisplayText(): String {
    return when (this) {
        PermissionResult.Granted -> "Granted"
        PermissionResult.Denied -> "Denied"
        PermissionResult.PermanentlyDenied -> "Permanently denied"
        PermissionResult.Cancelled -> "Cancelled"
    }
}

private const val SDK_DEBUG_PREFS_NAME = "android_permission_sdk_flags"

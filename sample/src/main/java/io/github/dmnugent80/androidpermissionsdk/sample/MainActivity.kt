package io.github.dmnugent80.androidpermissionsdk.sample

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dmnugent80.androidpermissionsdk.api.AndroidPermissionSdkFactory
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus

class MainActivity : ComponentActivity() {
    private val sdk by lazy { AndroidPermissionSdkFactory.create(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this
        setContent {
            val permissionViewModel: PermissionSampleViewModel = viewModel(
                factory = PermissionSampleViewModelFactory(sdk)
            )
            MaterialTheme {
                Scaffold { padding ->
                    PermissionSampleScreen(
                        viewModel = permissionViewModel,
                        activity = activity,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionSampleScreen(
    viewModel: PermissionSampleViewModel,
    activity: ComponentActivity,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel, activity) {
        viewModel.refreshAll(activity)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAll(activity)
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
            status = uiState.camera.status,
            shouldShowEducation = uiState.camera.shouldShowEducation,
            lastResult = uiState.camera.lastResult,
            onRefresh = { viewModel.refreshAll(activity) },
            onMarkEducationShown = viewModel::markCameraEducationShown,
            onRequest = { viewModel.requestCamera(activity) }
        )

        PermissionSection(
            title = "Fine Location",
            status = uiState.location.status,
            shouldShowEducation = uiState.location.shouldShowEducation,
            lastResult = uiState.location.lastResult,
            onRefresh = { viewModel.refreshAll(activity) },
            onMarkEducationShown = viewModel::markLocationEducationShown,
            onRequest = { viewModel.requestLocation(activity) }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.clearDebugState(context = activity, activity = activity)
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
    onRequest: () -> Unit
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
            status.toExplanationText()?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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

            Button(onClick = onMarkEducationShown) {
                Text("Mark education shown")
            }
        }
    }
}

private fun PermissionStatus.toDisplayText(): String {
    return when (this) {
        PermissionStatus.Granted -> "Granted"
        PermissionStatus.NotRequestedYet -> "Not requested yet"
        PermissionStatus.Denied -> "Denied"
    }
}

private fun PermissionResult.toDisplayText(): String {
    return when (this) {
        PermissionResult.Granted -> "Granted"
        PermissionResult.Denied -> "Denied"
        PermissionResult.Cancelled -> "Cancelled"
    }
}

internal fun PermissionStatus.toExplanationText(): String? {
    return when (this) {
        PermissionStatus.Denied -> {
            "Denied means not currently granted. This can happen after an explicit deny, " +
                "one-time grant expiration, or a settings revoke."
        }
        PermissionStatus.Granted,
        PermissionStatus.NotRequestedYet -> null
    }
}

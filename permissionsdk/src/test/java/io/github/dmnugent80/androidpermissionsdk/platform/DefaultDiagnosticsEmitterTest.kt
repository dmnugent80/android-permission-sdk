package io.github.dmnugent80.androidpermissionsdk.platform

import io.github.dmnugent80.androidpermissionsdk.api.AppPermission
import io.github.dmnugent80.androidpermissionsdk.api.PermissionResult
import io.github.dmnugent80.androidpermissionsdk.api.PermissionSdkConfig
import org.junit.Assert.fail
import org.junit.Test

class DefaultDiagnosticsEmitterTest {

    @Test
    fun `emit methods do not throw when listener throws`() {
        val emitter = DefaultDiagnosticsEmitter(
            PermissionSdkConfig.Builder()
                .eventListener { error("listener boom") }
                .debugMode(false)
                .build()
        )

        assertNoThrow {
            emitter.emitRequestStarted(AppPermission.Camera)
            emitter.emitLauncherRegistrationFailed(AppPermission.Camera, IllegalStateException("register fail"))
            emitter.emitSystemResponseReceived(
                AppPermission.Camera,
                mapOf(android.Manifest.permission.CAMERA to true)
            )
            emitter.emitRequestCompleted(AppPermission.Camera, PermissionResult.Granted)
        }
    }

    @Test
    fun `emit methods do not throw when logger throws`() {
        val emitter = DefaultDiagnosticsEmitter(
            PermissionSdkConfig.Builder()
                .logger { _, _, _, _ -> error("logger boom") }
                .debugMode(true)
                .build()
        )

        assertNoThrow {
            emitter.emitRequestStarted(AppPermission.Camera)
            emitter.emitLauncherRegistrationFailed(AppPermission.Camera, IllegalStateException("register fail"))
            emitter.emitSystemResponseReceived(
                AppPermission.Camera,
                mapOf(android.Manifest.permission.CAMERA to true)
            )
            emitter.emitRequestCompleted(AppPermission.Camera, PermissionResult.Granted)
        }
    }

    private fun assertNoThrow(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            fail("Expected no exception but got: ${t.message}")
        }
    }
}

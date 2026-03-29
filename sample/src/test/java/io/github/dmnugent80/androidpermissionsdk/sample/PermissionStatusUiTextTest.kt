package io.github.dmnugent80.androidpermissionsdk.sample

import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PermissionStatusUiTextTest {
    @Test
    fun denied_canRetry_explanation_mentions_rationale() {
        val explanation = PermissionStatus.Denied(canRequestAgain = true).toExplanationText()

        assertEquals(
            "User denied but can be asked again. Show rationale before requesting.",
            explanation
        )
    }

    @Test
    fun denied_permanent_explanation_mentions_settings() {
        val explanation = PermissionStatus.Denied(canRequestAgain = false).toExplanationText()

        assertEquals(
            "User denied permanently. Redirect to app settings to grant.",
            explanation
        )
    }

    @Test
    fun nonDenied_statuses_have_no_extra_explanation() {
        assertNull(PermissionStatus.Granted.toExplanationText())
        assertNull(PermissionStatus.NotRequestedYet.toExplanationText())
    }

    @Test
    fun error_statuses_have_explanations() {
        assertEquals(
            "Permission not declared in AndroidManifest.xml.",
            PermissionStatus.MissingFromManifest.toExplanationText()
        )
        assertEquals(
            "Permission not available on this Android version.",
            PermissionStatus.UnavailableOnApiLevel.toExplanationText()
        )
        assertEquals(
            "A request is already in progress.",
            PermissionStatus.RequestInProgress.toExplanationText()
        )
    }
}

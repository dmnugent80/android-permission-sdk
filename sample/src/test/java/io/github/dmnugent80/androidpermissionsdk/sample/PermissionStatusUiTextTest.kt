package io.github.dmnugent80.androidpermissionsdk.sample

import io.github.dmnugent80.androidpermissionsdk.api.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PermissionStatusUiTextTest {
    @Test
    fun denied_explanation_mentions_oneTimeExpiryAndRevoke() {
        val explanation = PermissionStatus.Denied.toExplanationText()

        assertEquals(
            "Denied means not currently granted. This can happen after an explicit deny, " +
                "one-time grant expiration, or a settings revoke.",
            explanation
        )
    }

    @Test
    fun nonDenied_statuses_have_no_extra_explanation() {
        assertNull(PermissionStatus.Granted.toExplanationText())
        assertNull(PermissionStatus.NotRequestedYet.toExplanationText())
    }
}

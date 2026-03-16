package io.github.dmnugent80.androidpermissionsdk.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermanentDenialPolicyTest {
    private val policy = PermanentDenialPolicy()

    @Test
    fun `returns false when no history exists`() {
        assertFalse(policy.isPermanentlyDenied(false, shouldShowRationale = false))
    }

    @Test
    fun `returns false when rationale is true`() {
        assertFalse(policy.isPermanentlyDenied(true, shouldShowRationale = true))
    }

    @Test
    fun `returns true when history exists and rationale is false`() {
        assertTrue(policy.isPermanentlyDenied(true, shouldShowRationale = false))
    }
}

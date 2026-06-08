package io.github.bengidev.openzone.home.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [ComposerReasoningLevel] — verifies the Off value exists and
 * the domain→wire effort mapping is correct (issue #7 acceptance criteria:
 * "Reasoning level extended with Off; existing Low/Medium/High preserved" and
 * "Off omits reasoning parameter; low/medium/high map to provider effort").
 */
class ComposerReasoningLevelTest {

    @Test
    fun `Off is the first entry and Low Medium High are preserved`() {
        assertEquals(
            listOf("Off", "Low", "Medium", "High"),
            ComposerReasoningLevel.entries.map { it.title }
        )
    }

    @Test
    fun `Off maps to null wire effort so no reasoning parameter is sent`() {
        assertNull(ComposerReasoningLevel.Off.wireEffort)
    }

    @Test
    fun `Low Medium High map to provider effort strings`() {
        assertEquals("low", ComposerReasoningLevel.Low.wireEffort)
        assertEquals("medium", ComposerReasoningLevel.Medium.wireEffort)
        assertEquals("high", ComposerReasoningLevel.High.wireEffort)
    }
}

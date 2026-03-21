package jp.example.autophone

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoAnswerPrefsTest {

    private lateinit var prefs: AutoAnswerPrefs

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = AutoAnswerPrefs(context)
    }

    @Test
    fun `デフォルトは自動応答OFF`() {
        assertFalse(prefs.isEnabled)
    }

    @Test
    fun `自動応答を有効にできる`() {
        prefs.isEnabled = true
        assertTrue(prefs.isEnabled)
    }

    @Test
    fun `デフォルトの応答遅延は2秒`() {
        assertEquals(2000L, prefs.answerDelayMs)
    }

    @Test
    fun `応答遅延を変更できる`() {
        prefs.answerDelayMs = 5000L
        assertEquals(5000L, prefs.answerDelayMs)
    }

    @Test
    fun `デフォルトの切断待機は1秒`() {
        assertEquals(1000L, prefs.hangUpAfterMs)
    }
}

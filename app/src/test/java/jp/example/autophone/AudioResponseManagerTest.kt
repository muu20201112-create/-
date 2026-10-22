package jp.example.autophone

import android.content.Context
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowTextToSpeech
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AudioResponseManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `録音未設定時は日本語で読み上げて一度だけ完了する`() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.JAPAN)
        var completions = 0
        AudioResponseManager(context).play { completions++ }
        val engine = shadowOf(ShadowTextToSpeech.getLastTextToSpeechInstance())
        engine.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(context.getString(R.string.default_greeting), engine.lastSpokenText)
        assertEquals(Locale.JAPAN, engine.currentLanguage)
        assertEquals(1, completions)
        assertTrue(engine.isShutdown)
    }

    @Test
    fun `日本語が使えない場合も完了しエンジンを解放する`() {
        var completions = 0
        AudioResponseManager(context).play { completions++ }
        val engine = shadowOf(ShadowTextToSpeech.getLastTextToSpeechInstance())
        engine.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, completions)
        assertTrue(engine.isShutdown)
    }

    @Test
    fun `停止後の初期化通知では読み上げや完了通知を行わない`() {
        ShadowTextToSpeech.addLanguageAvailability(Locale.JAPAN)
        var completions = 0
        val manager = AudioResponseManager(context)
        manager.play { completions++ }
        val engine = shadowOf(ShadowTextToSpeech.getLastTextToSpeechInstance())
        manager.stop()
        engine.onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0, completions)
        assertTrue(engine.isShutdown)
        assertTrue(engine.spokenTextList.isEmpty())
    }
}

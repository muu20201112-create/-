package jp.example.autophone

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppResourcesTest {
    @Test
    fun `メイン画面が起動できる`() {
        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            assertNotNull(activity.findViewById<android.view.View>(R.id.switch_auto_answer))
        }
    }

    @Test
    fun `ランチャーのアイコンとテーマが解決できる`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context.getDrawable(R.mipmap.ic_launcher))
        assertNotNull(context.getDrawable(R.mipmap.ic_launcher_round))
        context.setTheme(R.style.Theme_AutoPhone)
        assertTrue(context.getString(R.string.default_greeting).contains("後ほど"))
    }
}

package jp.example.autophone

import android.content.Context
import android.content.SharedPreferences

/**
 * 自動応答の設定を SharedPreferences で管理する。
 */
class AutoAnswerPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auto_answer_prefs", Context.MODE_PRIVATE)

    /** 自動応答の有効/無効 */
    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** 着信から応答までの待機時間（ミリ秒）*/
    var answerDelayMs: Long
        get() = prefs.getLong(KEY_ANSWER_DELAY_MS, DEFAULT_ANSWER_DELAY_MS)
        set(value) = prefs.edit().putLong(KEY_ANSWER_DELAY_MS, value).apply()

    /** メッセージ再生後に切断するまでの待機時間（ミリ秒）*/
    var hangUpAfterMs: Long
        get() = prefs.getLong(KEY_HANGUP_AFTER_MS, DEFAULT_HANGUP_AFTER_MS)
        set(value) = prefs.edit().putLong(KEY_HANGUP_AFTER_MS, value).apply()

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ANSWER_DELAY_MS = "answer_delay_ms"
        private const val KEY_HANGUP_AFTER_MS = "hangup_after_ms"

        const val DEFAULT_ANSWER_DELAY_MS = 2000L   // 2秒後に応答
        const val DEFAULT_HANGUP_AFTER_MS = 1000L   // メッセージ再生後1秒で切断
    }
}

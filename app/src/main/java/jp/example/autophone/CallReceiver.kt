package jp.example.autophone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log

/**
 * 着信検知用 BroadcastReceiver。
 * InCallService が使えない場合（デフォルト電話アプリ未設定時）の
 * フォールバックとして ANSWER_PHONE_CALLS パーミッションで応答する。
 */
class CallReceiver : BroadcastReceiver() {

    private val tag = "CallReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            val prefs = AutoAnswerPrefs(context)
            if (!prefs.isEnabled) return

            Log.d(tag, "着信検知（BroadcastReceiver）")

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                answerCall(context)
            }, prefs.answerDelayMs)
        }
    }

    private fun answerCall(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        try {
            @Suppress("DEPRECATION")
            telecomManager.acceptRingingCall()
            Log.d(tag, "自動応答完了（TelecomManager）")

            AudioResponseManager(context).play(onComplete = {})
        } catch (e: SecurityException) {
            Log.e(tag, "ANSWER_PHONE_CALLS パーミッションが不足しています: ${e.message}")
        }
    }
}

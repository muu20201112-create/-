package jp.example.autophone

import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log

/**
 * 着信を自動で受け、音声メッセージを再生する InCallService。
 * デフォルト電話アプリとして設定された場合に動作する。
 */
class AutoAnswerInCallService : InCallService() {

    private val tag = "AutoAnswerInCallService"

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (state == Call.STATE_RINGING) {
                handleIncomingCall(call)
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(tag, "着信: ${call.details?.handle}")

        call.registerCallback(callCallback)

        if (call.state == Call.STATE_RINGING) {
            handleIncomingCall(call)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
    }

    private fun handleIncomingCall(call: Call) {
        val prefs = AutoAnswerPrefs(applicationContext)
        if (!prefs.isEnabled) {
            Log.d(tag, "自動応答は無効です")
            return
        }

        val delayMs = prefs.answerDelayMs
        Log.d(tag, "${delayMs}ms 後に自動応答します")

        android.os.Handler(mainLooper).postDelayed({
            if (call.state == Call.STATE_RINGING) {
                answerAndPlayMessage(call)
            }
        }, delayMs)
    }

    private fun answerAndPlayMessage(call: Call) {
        Log.d(tag, "自動応答開始")
        call.answer(VideoProfile.STATE_AUDIO_ONLY)

        val prefs = AutoAnswerPrefs(applicationContext)
        val hangUpAfterMs = prefs.hangUpAfterMs

        AudioResponseManager(applicationContext).play(
            onComplete = {
                Log.d(tag, "メッセージ再生完了 → ${hangUpAfterMs}ms 後に切断")
                android.os.Handler(mainLooper).postDelayed({
                    call.disconnect()
                }, hangUpAfterMs)
            }
        )
    }
}

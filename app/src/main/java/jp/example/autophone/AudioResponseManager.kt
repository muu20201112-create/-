package jp.example.autophone

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/** 録音した res/raw/greeting を優先し、未設定時は日本語読み上げを使用する。 */
class AudioResponseManager(private val context: Context) {
    private val tag = "AudioResponseManager"
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var speech: TextToSpeech? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var completion: (() -> Unit)? = null
    private var generation = 0

    fun play(onComplete: () -> Unit) {
        stop()
        completion = onComplete
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .build()
        audioFocusRequest = focusRequest
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(tag, "オーディオフォーカス取得失敗")
        }

        val recording = context.resources.getIdentifier("greeting", "raw", context.packageName)
        if (recording == 0) {
            speakGreeting(attributes)
            return
        }
        try {
            val player = MediaPlayer.create(context, recording, attributes, 0)
            if (player == null) {
                Log.e(tag, "録音音声を開けません")
                finish()
                return
            }
            mediaPlayer = player
            player.setOnCompletionListener { finish() }
            player.setOnErrorListener { _, what, extra ->
                Log.e(tag, "音声再生エラー: what=$what extra=$extra")
                finish()
                true
            }
            player.start()
        } catch (error: RuntimeException) {
            Log.e(tag, "録音音声の再生に失敗", error)
            finish()
        }
    }

    private fun speakGreeting(attributes: AudioAttributes) {
        val playback = generation
        speech = TextToSpeech(context) { status ->
            // 初期化と完了通知をメインスレッドにそろえ、停止後の通知を無視する。
            handler.post {
                if (generation != playback) return@post
                val engine = speech ?: return@post
                if (status != TextToSpeech.SUCCESS) {
                    Log.e(tag, "読み上げエンジンの初期化に失敗")
                    finish()
                    return@post
                }
                val language = engine.setLanguage(Locale.JAPAN)
                if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(tag, "端末の日本語読み上げが利用できません")
                    finish()
                    return@post
                }
                engine.setAudioAttributes(attributes)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) {
                        handler.post { if (speech === engine) finish() }
                    }
                    @Deprecated("Legacy TextToSpeech callback")
                    override fun onError(utteranceId: String?) {
                        handler.post {
                            if (speech === engine) {
                                Log.e(tag, "読み上げに失敗")
                                finish()
                            }
                        }
                    }
                })
                if (engine.speak(context.getString(R.string.default_greeting), TextToSpeech.QUEUE_FLUSH,
                        null, "auto-phone-greeting") == TextToSpeech.ERROR) {
                    Log.e(tag, "読み上げの開始に失敗")
                    finish()
                }
            }
        }
    }

    private fun finish() {
        val callback = completion
        stop()
        callback?.invoke()
    }

    fun stop() {
        generation++
        completion = null
        mediaPlayer?.release()
        mediaPlayer = null
        val engine = speech
        speech = null
        engine?.stop()
        engine?.shutdown()
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }
}

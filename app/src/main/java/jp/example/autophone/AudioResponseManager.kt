package jp.example.autophone

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

/**
 * 自動応答時の音声メッセージを再生する。
 * res/raw/greeting.mp3 を使用。差し替えることでメッセージを変更できる。
 */
class AudioResponseManager(private val context: Context) {

    private val tag = "AudioResponseManager"
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    fun play(onComplete: () -> Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 通話中のスピーカー出力にフォーカスを要求
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .build()

        audioFocusRequest = focusRequest

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(tag, "オーディオフォーカス取得失敗")
        }

        mediaPlayer = MediaPlayer.create(context, R.raw.greeting)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            setOnCompletionListener {
                Log.d(tag, "音声再生完了")
                release()
                mediaPlayer = null
                audioFocusRequest?.let { req -> audioManager.abandonAudioFocusRequest(req) }
                onComplete()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(tag, "音声再生エラー: what=$what extra=$extra")
                release()
                mediaPlayer = null
                onComplete()
                true
            }
            start()
            Log.d(tag, "音声メッセージ再生開始")
        } ?: run {
            Log.e(tag, "res/raw/greeting.mp3 が見つかりません")
            onComplete()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}

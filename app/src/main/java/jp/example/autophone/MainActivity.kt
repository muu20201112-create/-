package jp.example.autophone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import jp.example.autophone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AutoAnswerPrefs

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            updateUi()
        } else {
            Toast.makeText(this, "パーミッションが必要です", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AutoAnswerPrefs(this)

        binding.switchAutoAnswer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableAutoAnswer()
            } else {
                prefs.isEnabled = false
                updateUi()
            }
        }

        binding.btnDefaultPhoneApp.setOnClickListener {
            openDefaultPhoneAppSettings()
        }

        updateUi()
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun enableAutoAnswer() {
        if (!hasRequiredPermissions()) {
            permissionLauncher.launch(requiredPermissions)
            binding.switchAutoAnswer.isChecked = false
            return
        }
        prefs.isEnabled = true
        updateUi()
    }

    private fun updateUi() {
        val isDefaultPhone = isDefaultPhoneApp()
        val enabled = prefs.isEnabled

        binding.switchAutoAnswer.isChecked = enabled
        binding.tvStatus.text = when {
            !hasRequiredPermissions() -> "パーミッション未付与"
            !isDefaultPhone -> "デフォルト電話アプリ未設定（推奨）"
            enabled -> "自動応答 ON"
            else -> "自動応答 OFF"
        }

        binding.btnDefaultPhoneApp.isEnabled = !isDefaultPhone
    }

    private fun hasRequiredPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun isDefaultPhoneApp(): Boolean {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        return telecomManager.defaultDialerPackage == packageName
    }

    private fun openDefaultPhoneAppSettings() {
        AlertDialog.Builder(this)
            .setTitle("デフォルト電話アプリの設定")
            .setMessage(
                "このアプリをデフォルト電話アプリに設定すると、" +
                "着信を確実に制御できます。\n\n" +
                "設定 → アプリ → デフォルトアプリ → 電話アプリ から変更してください。"
            )
            .setPositiveButton("設定を開く") { _, _ ->
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                startActivity(intent)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
}

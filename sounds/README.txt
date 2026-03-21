音声ファイル配置ガイド
======================

以下のカスタム音声ファイルを作成し、
/var/lib/asterisk/sounds/ja/custom/ に配置してください。

必要なファイル（.wav または .gsm 形式）:
  greeting.wav        - 挨拶メッセージ
                        例: "お電話ありがとうございます。自動応答システムにつながりました。"

  menu.wav            - メニュー案内
                        例: "1番で営業時間、2番で住所、3番でオペレーター、0番でメニューに戻ります。"

  business_hours.wav  - 営業時間案内
                        例: "営業時間は平日9時から18時です。"

  address.wav         - 住所案内
                        例: "所在地は東京都千代田区丸の内一丁目です。"

  transfer.wav        - 転送案内
                        例: "オペレーターにおつなぎします。しばらくお待ちください。"

  goodbye.wav         - 終話メッセージ
                        例: "お電話ありがとうございました。"

  invalid_input.wav   - 無効入力時
                        例: "入力が確認できませんでした。"

  operator_busy.wav   - オペレーター話中時
                        例: "只今対応中です。後ほどおかけ直しください。"

  operator_noanswer.wav - 応答なし時
                        例: "只今応答できません。後ほどおかけ直しください。"

  error.wav           - システムエラー時
                        例: "システムエラーが発生しました。"

音声ファイル生成ツール（TTS）:
  - AquesTalk / VOICEVOX（日本語 TTS）
  - Amazon Polly（日本語対応クラウド TTS）
  - espeak-ng（オープンソース）
  - sox コマンドで変換: sox input.mp3 -r 8000 -c 1 output.wav

Asterisk 対応フォーマット:
  - PCM 8kHz 16bit モノラル .wav（推奨）
  - GSM 8kHz .gsm

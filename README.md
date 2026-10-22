# AutoPhone

Android 8.0 以降向けの電話自動応答アプリです。

## ビルドとテスト

JDK 17、Gradle 8.5、Android SDK 34、Build Tools 34.0.0 を使用します。
`ANDROID_HOME` に SDK の場所を指定してください。

```sh
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

APK は `app/build/outputs/apk/debug/app-debug.apk` に生成されます。

準備済みのクラウド環境では次の手順を使います。

```sh
source /workspace/setup/activate.sh
python /workspace/setup/java-network.py
cd /workspace/-
gradle --no-daemon -I /workspace/setup/cloud-tests.gradle :app:assembleDebug :app:testDebugUnitTest
```

## 応答音声

`app/src/main/res/raw/greeting.mp3`（または Android が対応する同名の音声ファイル）
があれば、その録音を再生します。未配置時は端末の日本語読み上げエンジンで
`default_greeting` のメッセージを読み上げます。端末に日本語音声データが必要です。
読み上げが利用できない場合はログに記録し、完了処理へ進みます。

## 実機での確認

必要な電話権限とデフォルト電話アプリの設定を行い、別の電話から着信させて確認してください。
クラウドの単体テストは着信・自動応答や通話相手への音声伝送を保証しません。
通常の Android アプリによるスピーカー再生は、通話の送話音声に直接注入されません。
相手側でメッセージが聞こえるかは端末と通話経路で検証が必要です。

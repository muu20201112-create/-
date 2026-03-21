"""
電話自動応答システム (Automatic Phone Response System)
Twilio を使用した電話自動応答アプリケーション
"""

from flask import Flask, request, Response
from twilio.twiml.voice_response import VoiceResponse, Gather
import os

app = Flask(__name__)

# 自動応答メッセージ設定
GREETING_MESSAGE = os.getenv(
    "GREETING_MESSAGE",
    "お電話ありがとうございます。自動応答システムにつながりました。"
)

MENU_MESSAGE = (
    "ご用件をお選びください。"
    "1番を押すと営業時間のご案内です。"
    "2番を押すとご住所のご案内です。"
    "3番を押すとオペレーターにおつなぎします。"
    "0番を押すともう一度メニューをお聞きになれます。"
)

BUSINESS_HOURS_MESSAGE = (
    "営業時間は平日の午前9時から午後6時までです。"
    "土曜日は午前10時から午後5時までです。"
    "日曜日および祝日は休業しております。"
)

ADDRESS_MESSAGE = (
    "所在地は東京都千代田区丸の内一丁目です。"
    "最寄り駅はJR東京駅丸の内口より徒歩3分です。"
)

TRANSFER_MESSAGE = "オペレーターにおつなぎします。しばらくお待ちください。"

INVALID_INPUT_MESSAGE = "入力が確認できませんでした。もう一度お試しください。"

GOODBYE_MESSAGE = "お電話ありがとうございました。またのお電話をお待ちしております。"


@app.route("/voice", methods=["GET", "POST"])
def voice():
    """着信時のエントリーポイント"""
    response = VoiceResponse()

    gather = Gather(num_digits=1, action="/menu", method="POST", timeout=10)
    gather.say(GREETING_MESSAGE, language="ja-JP")
    gather.say(MENU_MESSAGE, language="ja-JP")
    response.append(gather)

    # タイムアウト時の処理
    response.say(GOODBYE_MESSAGE, language="ja-JP")
    response.hangup()

    return Response(str(response), mimetype="text/xml")


@app.route("/menu", methods=["GET", "POST"])
def menu():
    """メニュー選択の処理"""
    digit = request.form.get("Digits", "")
    response = VoiceResponse()

    if digit == "1":
        # 営業時間案内
        response.say(BUSINESS_HOURS_MESSAGE, language="ja-JP")
        _add_return_to_menu(response)

    elif digit == "2":
        # 住所案内
        response.say(ADDRESS_MESSAGE, language="ja-JP")
        _add_return_to_menu(response)

    elif digit == "3":
        # オペレーター転送
        operator_number = os.getenv("OPERATOR_NUMBER")
        response.say(TRANSFER_MESSAGE, language="ja-JP")
        if operator_number:
            response.dial(operator_number)
        else:
            response.say(
                "現在オペレーターが対応できません。後ほどおかけ直しください。",
                language="ja-JP"
            )
            response.say(GOODBYE_MESSAGE, language="ja-JP")
        response.hangup()

    elif digit == "0":
        # メニューに戻る
        return voice()

    else:
        # 無効な入力
        gather = Gather(num_digits=1, action="/menu", method="POST", timeout=10)
        gather.say(INVALID_INPUT_MESSAGE, language="ja-JP")
        gather.say(MENU_MESSAGE, language="ja-JP")
        response.append(gather)
        response.say(GOODBYE_MESSAGE, language="ja-JP")
        response.hangup()

    return Response(str(response), mimetype="text/xml")


@app.route("/status", methods=["POST"])
def call_status():
    """通話ステータスコールバック"""
    call_sid = request.form.get("CallSid", "")
    call_status = request.form.get("CallStatus", "")
    from_number = request.form.get("From", "")

    app.logger.info(
        f"通話ステータス更新: SID={call_sid}, 状態={call_status}, 発信元={from_number}"
    )

    return Response("OK", status=200)


def _add_return_to_menu(response: VoiceResponse) -> None:
    """メニューに戻る選択肢を追加"""
    gather = Gather(num_digits=1, action="/menu", method="POST", timeout=10)
    gather.say(
        "メインメニューに戻るには0を押してください。終了するには何も押さずにお待ちください。",
        language="ja-JP"
    )
    response.append(gather)
    response.say(GOODBYE_MESSAGE, language="ja-JP")
    response.hangup()


if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    debug = os.getenv("DEBUG", "false").lower() == "true"
    app.run(host="0.0.0.0", port=port, debug=debug)

#!/usr/bin/env python3
"""
電話自動応答システム - Asterisk AGI スクリプト
Asterisk Gateway Interface (AGI) を使用した電話自動応答
"""

import sys
import os
import logging

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from asterisk.agi import AGI

# ログ設定
logging.basicConfig(
    filename="/var/log/asterisk/auto_response.log",
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)
logger = logging.getLogger(__name__)

# 音声ファイルのパス（Asterisk サウンドディレクトリ）
SOUND_DIR = "/var/lib/asterisk/sounds/ja"

# DTMF タイムアウト（秒）
DIGIT_TIMEOUT = 10

# メニュー定義
MENU_OPTIONS = {
    "1": "business_hours",
    "2": "address",
    "3": "operator",
    "0": "main_menu",
}


def play_and_wait(agi: AGI, sound_file: str, timeout: int = DIGIT_TIMEOUT) -> str:
    """音声を再生して DTMF 入力を待つ。入力された数字を返す。"""
    result = agi.stream_file(sound_file, escape_digits="0123456789#*", timeout=timeout)
    return result or ""


def main_menu(agi: AGI) -> str:
    """メインメニューを再生して選択を受け付ける"""
    while True:
        agi.verbose("メインメニュー再生", 2)

        # 挨拶 + メニュー案内
        digit = play_and_wait(agi, "custom/greeting")
        if not digit:
            digit = play_and_wait(agi, "custom/menu")

        if digit in MENU_OPTIONS:
            return MENU_OPTIONS[digit]

        # 無効入力
        agi.stream_file("custom/invalid_input")


def handle_business_hours(agi: AGI) -> str:
    """営業時間案内"""
    agi.verbose("営業時間案内", 2)
    digit = play_and_wait(agi, "custom/business_hours")

    if digit == "0":
        return "main_menu"
    return "goodbye"


def handle_address(agi: AGI) -> str:
    """住所案内"""
    agi.verbose("住所案内", 2)
    digit = play_and_wait(agi, "custom/address")

    if digit == "0":
        return "main_menu"
    return "goodbye"


def handle_operator(agi: AGI) -> str:
    """オペレーター転送"""
    agi.verbose("オペレーター転送", 2)
    operator_extension = os.getenv("OPERATOR_EXTENSION", "100")

    agi.stream_file("custom/transfer")
    agi.set_variable("TRANSFER_DEST", operator_extension)

    # ダイアルプランでの転送のために変数をセット
    agi.set_variable("AUTO_RESPONSE_ACTION", "transfer")
    return "exit"


def run(agi: AGI) -> None:
    """メイン処理ループ"""
    caller_id = agi.env.get("agi_callerid", "unknown")
    logger.info(f"着信: {caller_id}")

    try:
        agi.answer()
        agi.set_variable("CHANNEL(language)", "ja")

        action = "main_menu"

        while action not in ("goodbye", "exit"):
            if action == "main_menu":
                action = main_menu(agi)
            elif action == "business_hours":
                action = handle_business_hours(agi)
            elif action == "address":
                action = handle_address(agi)
            elif action == "operator":
                action = handle_operator(agi)
                break
            else:
                action = "goodbye"

        if action == "goodbye":
            agi.stream_file("custom/goodbye")
            agi.hangup()

        logger.info(f"通話終了: {caller_id}, アクション={action}")

    except Exception as e:
        logger.error(f"エラー発生: {e}", exc_info=True)
        agi.stream_file("custom/error")
        agi.hangup()


if __name__ == "__main__":
    agi = AGI()
    run(agi)

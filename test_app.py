"""
電話自動応答システムのテスト
"""

import pytest
from app import app


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def test_voice_endpoint_returns_xml(client):
    """着信エンドポイントが TwiML XML を返すことを確認"""
    response = client.post("/voice")
    assert response.status_code == 200
    assert b"<?xml" in response.data or b"<Response>" in response.data
    assert b"Gather" in response.data


def test_voice_contains_greeting(client):
    """挨拶メッセージが含まれることを確認"""
    response = client.post("/voice")
    assert b"ja-JP" in response.data


def test_menu_digit_1_business_hours(client):
    """1番で営業時間案内が返ることを確認"""
    response = client.post("/menu", data={"Digits": "1"})
    assert response.status_code == 200
    assert b"Response" in response.data


def test_menu_digit_2_address(client):
    """2番で住所案内が返ることを確認"""
    response = client.post("/menu", data={"Digits": "2"})
    assert response.status_code == 200
    assert b"Response" in response.data


def test_menu_digit_3_transfer(client):
    """3番でオペレーター転送処理が返ることを確認"""
    response = client.post("/menu", data={"Digits": "3"})
    assert response.status_code == 200
    assert b"Response" in response.data


def test_menu_digit_0_returns_to_main(client):
    """0番でメインメニューに戻ることを確認"""
    response = client.post("/menu", data={"Digits": "0"})
    assert response.status_code == 200
    assert b"Gather" in response.data


def test_menu_invalid_digit(client):
    """無効な入力でエラーメッセージが返ることを確認"""
    response = client.post("/menu", data={"Digits": "9"})
    assert response.status_code == 200
    assert b"Response" in response.data


def test_status_callback(client):
    """ステータスコールバックが OK を返すことを確認"""
    response = client.post("/status", data={
        "CallSid": "CAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "CallStatus": "completed",
        "From": "+81XXXXXXXXXX"
    })
    assert response.status_code == 200

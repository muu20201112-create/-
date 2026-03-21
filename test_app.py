"""
電話自動応答システムのテスト
"""

import sys
import os
from unittest.mock import MagicMock, patch, call
import pytest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))


@pytest.fixture
def mock_agi():
    agi = MagicMock()
    agi.env = {"agi_callerid": "09012345678"}
    return agi


class TestMainMenu:
    def test_selects_business_hours_on_digit_1(self, mock_agi):
        from agi.auto_response import main_menu
        mock_agi.stream_file.side_effect = ["1", ""]
        result = main_menu(mock_agi)
        assert result == "business_hours"

    def test_selects_address_on_digit_2(self, mock_agi):
        from agi.auto_response import main_menu
        mock_agi.stream_file.side_effect = ["", "2"]
        result = main_menu(mock_agi)
        assert result == "address"

    def test_selects_operator_on_digit_3(self, mock_agi):
        from agi.auto_response import main_menu
        mock_agi.stream_file.side_effect = ["", "3"]
        result = main_menu(mock_agi)
        assert result == "operator"

    def test_returns_to_main_menu_on_digit_0(self, mock_agi):
        from agi.auto_response import main_menu
        # 0 → 無効ループを防ぐため2回目に有効入力
        mock_agi.stream_file.side_effect = ["", "0", "", "1"]
        result = main_menu(mock_agi)
        assert result == "main_menu"

    def test_retries_on_invalid_input(self, mock_agi):
        from agi.auto_response import main_menu
        mock_agi.stream_file.side_effect = ["", "9", "", "1"]
        result = main_menu(mock_agi)
        assert result == "business_hours"


class TestHandleBusinessHours:
    def test_returns_goodbye_by_default(self, mock_agi):
        from agi.auto_response import handle_business_hours
        mock_agi.stream_file.return_value = ""
        result = handle_business_hours(mock_agi)
        assert result == "goodbye"

    def test_returns_main_menu_on_digit_0(self, mock_agi):
        from agi.auto_response import handle_business_hours
        mock_agi.stream_file.return_value = "0"
        result = handle_business_hours(mock_agi)
        assert result == "main_menu"


class TestHandleAddress:
    def test_returns_goodbye_by_default(self, mock_agi):
        from agi.auto_response import handle_address
        mock_agi.stream_file.return_value = ""
        result = handle_address(mock_agi)
        assert result == "goodbye"

    def test_returns_main_menu_on_digit_0(self, mock_agi):
        from agi.auto_response import handle_address
        mock_agi.stream_file.return_value = "0"
        result = handle_address(mock_agi)
        assert result == "main_menu"


class TestHandleOperator:
    def test_sets_transfer_action(self, mock_agi):
        from agi.auto_response import handle_operator
        mock_agi.stream_file.return_value = ""
        result = handle_operator(mock_agi)
        assert result == "exit"
        mock_agi.set_variable.assert_any_call("AUTO_RESPONSE_ACTION", "transfer")

    @patch.dict(os.environ, {"OPERATOR_EXTENSION": "200"})
    def test_uses_env_operator_extension(self, mock_agi):
        from agi.auto_response import handle_operator
        mock_agi.stream_file.return_value = ""
        handle_operator(mock_agi)
        mock_agi.set_variable.assert_any_call("TRANSFER_DEST", "200")


class TestRun:
    def test_full_flow_goodbye(self, mock_agi):
        from agi.auto_response import run
        # greeting → digit 1 (業務時間) → no digit → goodbye
        mock_agi.stream_file.side_effect = ["1", "", ""]
        run(mock_agi)
        mock_agi.answer.assert_called_once()
        mock_agi.hangup.assert_called()

    def test_error_handling(self, mock_agi):
        from agi.auto_response import run
        mock_agi.answer.side_effect = Exception("接続エラー")
        run(mock_agi)
        mock_agi.hangup.assert_called()

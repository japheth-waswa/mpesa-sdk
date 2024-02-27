package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrxCodeType {
    BUY_GOODS("BG"),PAY_BILL("PB"),SEND_MONEY_MOBILE_NUMBER("SM"),SEND_MONEY_BUSINESS_SHORTCODE("SB"),WITHDRAW_CASH_AGENT_TILL("WA");
    private final String value;
}

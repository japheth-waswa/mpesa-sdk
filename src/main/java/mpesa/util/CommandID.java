package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandID {
    PAY_BILL("BusinessPayBill"),BUY_GOODS("BusinessBuyGoods");
    private final String value;
}

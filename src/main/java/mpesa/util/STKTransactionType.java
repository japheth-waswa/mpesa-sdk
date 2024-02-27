package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum STKTransactionType {
    PAY_BILL("CustomerPayBillOnline"),TILL_NUMBER("CustomerBuyGoodsOnline");

    private final String value;

}

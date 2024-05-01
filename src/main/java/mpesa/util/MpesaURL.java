package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MpesaURL {
    AUTH("oauth/v1/generate?grant_type=client_credentials"),
    STK_SEND("mpesa/stkpush/v1/processrequest"),
    STK_QUERY("mpesa/stkpushquery/v1/query"),
    C2B_REGISTER_URL("mpesa/c2b/v1/registerurl"),
    C2B_TRANSACTION_STATUS_URL("mpesa/transactionstatus/v1/query"),
    B2B_PAYMENT("mpesa/b2b/v1/paymentrequest"),
    B2B_STK("v1/ussdpush/get-msisdn"),
    B2C("mpesa/b2c/v3/paymentrequest"),
    TAX_REMITTANCE("mpesa/b2b/v1/remittax"),
    DYNAMIC_QR("mpesa/qrcode/v1/generate");
    private final String url;
}

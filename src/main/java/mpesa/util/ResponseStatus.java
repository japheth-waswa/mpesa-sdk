package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    SUCCESS("0"),
    INVALID_MSISDN("Invalid MSISDN"),
    INVALID_ACCOUNT_NUMBER("Invalid Account Number"),
    INVALID_AMOUNT("Invalid Amount"),
    INVALID_KYC_DETAILS("Invalid KYC Details"),
    INVALID_SHORTCODE("Invalid Shortcode"),
    OTHER_ERROR("Other Error");
    private final String value;
}

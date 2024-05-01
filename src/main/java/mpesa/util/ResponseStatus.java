package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    SUCCESS("0","Success"),
    INVALID_MSISDN("C2B00011","Invalid MSISDN"),
    INVALID_ACCOUNT_NUMBER("C2B00012","Invalid Account Number"),
    INVALID_AMOUNT("C2B00013","Invalid Amount"),
    INVALID_KYC_DETAILS("C2B00014","Invalid KYC Details"),
    INVALID_SHORTCODE("C2B00015","Invalid Shortcode"),
    OTHER_ERROR("C2B00016","Other Error");
    private final String value;
    private final String description;
}

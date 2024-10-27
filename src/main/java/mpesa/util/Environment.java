package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Environment {
    DEVELOPMENT("https://sandbox.safaricom.co.ke"),
    PRODUCTION("https://api.safaricom.co.ke");

    private final String value;

}

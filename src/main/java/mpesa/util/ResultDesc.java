package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultDesc {
    SUCCESS("Success"),ACCEPTED("Accepted"),REJECTED("Rejected");
    private final String value;
}

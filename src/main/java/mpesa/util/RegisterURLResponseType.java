package mpesa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RegisterURLResponseType {
    CANCELLED("Cancelled"),COMPLETED("Completed");

    private final String value;


}

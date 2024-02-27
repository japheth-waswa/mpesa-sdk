package mpesa.b2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResultParameter {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;
}

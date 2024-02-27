package mpesa.stk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Value")
    private String value;
}

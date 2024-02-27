package mpesa.b2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResponseBody {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("status")
    private String status;
}

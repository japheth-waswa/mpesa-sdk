package mpesa.b2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResultParameters {
    @JsonProperty("ResultParameter")
    private List<ResultParameter> resultParameter;
}

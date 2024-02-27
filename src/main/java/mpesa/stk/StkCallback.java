package mpesa.stk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StkCallback{
    @JsonProperty("MerchantRequestID")
    private String merchantRequestID;
    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestID;
    @JsonProperty("ResultCode")
    private Integer resultCode;
    @JsonProperty("ResultDesc")
    private String resultDesc;
    @JsonProperty("CallbackMetadata")
    private CallbackMetadata callbackMetadata;
}

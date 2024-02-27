package mpesa.b2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result {
    @JsonProperty("ResultType")
    private int resultType;

    @JsonProperty("ResultCode")
    private int resultCode;

    @JsonProperty("ResultDesc")
    private String resultDesc;

    @JsonProperty("OriginatorConversationID")
    private String originatorConversationId;

    @JsonProperty("ConversationID")
    private String conversationId;

    @JsonProperty("TransactionID")
    private String transactionId;

    @JsonProperty("ResultParameters")
    private ResultParameters resultParameters;

    @JsonProperty("ReferenceData")
    private ReferenceData referenceData;
}

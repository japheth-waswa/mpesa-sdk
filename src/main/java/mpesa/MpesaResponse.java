package mpesa;

import base.Response;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import mpesa.b2b.ResponseBody;
import mpesa.b2b.Result;
import mpesa.stk.Body;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static base.Helpers.MPESA_TIMESTAMP_FORMAT;

class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String dateTimeString = jsonParser.getText();
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(MPESA_TIMESTAMP_FORMAT));
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaResponse implements Response {
    //internal
    private boolean internalStatus;

    //Auth
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    //error
    private String requestId;
    private String errorCode;
    private String errorMessage;

    //others
    @JsonProperty("MerchantRequestID")
    private String merchantRequestID;

    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestID;

    @JsonProperty("RequestID")
    private String requestID;

    @JsonProperty("ResponseCode")
    private Integer responseCode;

    @JsonProperty("ResponseDescription")
    private String responseDescription;

    @JsonProperty("CustomerMessage")
    private String customerMessage;

    @JsonProperty("ResultCode")
    @JsonAlias(value = {"resultCode"})
    private String resultCode;

    @JsonProperty("ResultDesc")
    @JsonAlias(value = {"resultDesc"})
    private String resultDesc;

    @JsonProperty("Body")
    private Body body;

    @JsonProperty("OriginatorCoversationID")
    private String originatorCoversationId;

    @JsonProperty("OriginatorConversationID")
    private String originatorConversationId;

    @JsonProperty("ConversationID")
    @JsonAlias(value = {"conversationID"})
    private String conversationId;

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("TransID")
    private String transID;

    @JsonProperty("TransTime")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime transTime;

    @JsonProperty("TransAmount")
    private Double transAmount;

    @JsonProperty("BusinessShortCode")
    private Integer businessShortCode;

    @JsonProperty("BillRefNumber")
    private String billRefNumber;

    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;

    @JsonProperty("OrgAccountBalance")
    private Double orgAccountBalance;

    @JsonProperty("ThirdPartyTransID")
    private String thirdPartyTransID;

    @JsonProperty("MSISDN")
    private String phoneNumber;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("MiddleName")
    private String middleName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("Result")
    private Result result;

    @JsonProperty("ResponseBody")
    private ResponseBody responseBody;

    @JsonProperty("QRCode")
    private String qrCode;

    private Double amount;
    private String mpesaReference;
    private String paymentReference;
    private Integer resultType;
    private String transactionId;
    private String status;

}

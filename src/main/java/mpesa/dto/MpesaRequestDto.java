package mpesa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import mpesa.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MpesaRequestDto {

    private MpesaRequestType mpesaRequestType;
    private STKTransactionType stkTransactionType;
    private RegisterURLResponseType registerURLResponseType;
    private B2CCommandID b2CCommandID;
    private String TaxPRN;
    private TrxCodeType trxCodeType;
    private Integer agentTill;

    @JsonProperty("BusinessShortCode")
    private Integer businessShortCode;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestID;

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("PartyA")
    private Long partyA;

    @JsonProperty("PartyB")
    private Long partyB;

    @JsonProperty("PhoneNumber")
    private Long phoneNumber;

    @JsonProperty("CallBackURL")
    private String callbackURL;

    @JsonProperty("AccountReference")
    private String accountReference;

    @JsonProperty("TransactionDesc")
    private String transactionDesc;

    @JsonProperty("ShortCode")
    private Integer shortCode;

    @JsonProperty("ResponseType")
    private String responseType;

    @JsonProperty("ConfirmationURL")
    private String confirmationURL;

    @JsonProperty("ValidationURL")
    private String validationURL;

    @JsonProperty("ResultCode")
    private String resultCode;

    @JsonProperty("ResultDesc")
    private String resultDesc;

    @JsonProperty("ThirdPartyTransID")
    private String thirdPartyTransID;

    @JsonProperty("Initiator")
    private String initiator;

    @JsonProperty("InitiatorName")
    private String initiatorName;

    @JsonProperty("SecurityCredential")
    private String securityCredential;

    @JsonProperty("CommandID")
    private String commandId;

    @JsonProperty("SenderIdentifierType")
    private String senderIdentifierType;

    @JsonProperty("RecieverIdentifierType")
    private String recieverIdentifierType;

    @JsonProperty("Requester")
    private Long requester;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("QueueTimeOutURL")
    private String queueTimeOutURL;

    @JsonProperty("ResultURL")
    private String resultURL;

    @JsonProperty("Occassion")
    private String occassion;

    @JsonProperty("amount")
    private double amt;

    @JsonProperty("primaryShortCode")
    private Integer sendingPartyShortCode;

    @JsonProperty("receiverShortCode")
    private Integer receivingPartyShortCode;

    @JsonProperty("paymentRef")
    private String paymentRef;

    @JsonProperty("callbackUrl")
    private String callback;

    @JsonProperty("partnerName")
    private String receivingPartyName;

    @JsonProperty("RequestRefID")
    private String requestRefId;

    @JsonProperty("OriginatorConversationID")
    private String originatorConversationId;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("RefNo")
    private String refNo;

    @JsonProperty("TrxCode")
    private String trxCode;

    @JsonProperty("CPI")
    private String cpI;

    @JsonProperty("Size")
    private String size;
}

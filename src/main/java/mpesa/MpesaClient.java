package mpesa;

import base.ApiClient;
import base.Header;
import base.Helpers;
import lombok.Data;
import lombok.experimental.Accessors;
import mpesa.b2b.ResponseBody;
import mpesa.b2b.ResultParameter;
import mpesa.dto.MpesaRequestDto;
import mpesa.stk.Item;
import mpesa.util.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static base.Helpers.MPESA_TIMESTAMP_FORMAT;

@Data
@Accessors(chain = true, fluent = true)
public class MpesaClient {
    private static final double MIN_B2B_AMOUNT = 10;
    private final String TAX_COMMAND_ID = "PayTaxToKRA";
    private final String SENDER_IDENTIFIER_TYPE = "4";
    private final String RECEIVER_IDENTIFIER_TYPE = "4";
    private final long TAX_ORG_BUSINESS_SHORTCODE = 572572;
    private Environment environment;
    private String consumerKey;
    private String consumerSecret;
    private String passKey;
    private String initiatorName;
    private String initiatorPassword;
    private MpesaRequestDto mpesaRequestDto;


    private String generatePassword(@NotNull String businessShortCode, @NotNull String timestamp) {
        String password = businessShortCode + passKey + timestamp;
        return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
    }

    private MpesaRequest initialStkSetup(@NotNull MpesaRequestType mpesaRequestType) {
        String timestamp = Helpers.formatDateTime(MPESA_TIMESTAMP_FORMAT);
        //add extra properties
        mpesaRequestDto.setMpesaRequestType(mpesaRequestType);
        mpesaRequestDto.setPassword(generatePassword(mpesaRequestDto.getBusinessShortCode().toString(), timestamp));
        mpesaRequestDto.setTimestamp(timestamp);
        //add to MpesaRequest
        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        return mpesaRequest;
    }

    private ApiClient<MpesaRequest, MpesaResponse> buildRequest(@NotNull ApiClient<MpesaRequest, MpesaResponse> apiClient, @NotNull MpesaRequest mpesaRequest, @NotNull MpesaURL mpesaURL) throws Exception {
        String accessToken = generateAccessToken().getAccessToken();
        return apiClient.toBuilder()
                .reqClass(mpesaRequest)
                .responseClass(MpesaResponse.class)
                .baseUri(Helpers.buildUrl(null, environment.getValue(), mpesaURL.getUrl()).toString())
                .headers(Collections.singletonList(new Header("Authorization", Helpers.bindBearerToken(accessToken))))
                .build();
    }

    private void B2BPaymentValidation() throws NoSuchPaddingException, IllegalBlockSizeException, CertificateException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (initiatorPassword == null || initiatorPassword.isEmpty() || environment == null) {
            throw new RuntimeException("Initiator password and environment must be provided");
        }
        MpesaRequestType mpesaRequestType = mpesaRequestDto.getMpesaRequestType();
        List<String> allowedMpesaRequestTypes = List.of(MpesaRequestType.B2B_PAY_BILL.toString(), MpesaRequestType.B2B_BUY_GOODS.toString());
        if (!allowedMpesaRequestTypes.contains(mpesaRequestType.toString())) {
            throw new RuntimeException("Either B2B_PAY_BILL & B2B_BUY_GOODS allowed");
        }

        //generate security credentials
        String securityCredentials = Helpers.generateSecurityCredentials(initiatorPassword, environment == Environment.DEVELOPMENT ? Helpers.MPESA_CERT_DEV : Helpers.MPESA_CERT_PROD);
        mpesaRequestDto.setSecurityCredential(securityCredentials);

        mpesaRequestDto.setInitiator(initiatorName);
        mpesaRequestDto.setSenderIdentifierType("4");
        mpesaRequestDto.setRecieverIdentifierType("4");
    }

    /**
     * Generate access_token from DARAJA api
     *
     * @return MpesaResponse {accessToken, expiresIn}
     * @throws Exception
     */
    public MpesaResponse generateAccessToken() throws Exception {
        String basicAuth = Helpers.generateBasicAuth(consumerKey, consumerSecret);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> apiClientBuild = apiClient.toBuilder()
                     .responseClass(MpesaResponse.class)
                     .baseUri(Helpers.buildUrl(null, environment.getValue(), MpesaURL.AUTH.getUrl()).toString())
                     .headers(Collections.singletonList(new Header("Authorization", "Basic " + basicAuth)))
                     .build()) {
            MpesaResponse mpesaResponse = apiClientBuild.get();
            mpesaResponse.setInternalStatus(mpesaResponse.getAccessToken() != null);
            return mpesaResponse;
        }
    }

    /**
     * Initiates STK push request.
     *
     * @return MpesaResponse Success{ internalStatus=true, merchantRequestID, checkoutRequestID, responseCode, responseDescription, customerMessage } ||
     * MpesaResponse Failed{ internalStatus=false, requestId, errorCode, errorMessage }
     * @throws Exception if there is an error during the request
     */
    public MpesaResponse stkSend() throws Exception {
        MpesaRequest mpesaRequest = initialStkSetup(MpesaRequestType.STK_SEND);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.STK_SEND)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

    /**
     * Parses the response from across all callbacks and timeout urls  (CallBackURL | ResultURL | QueueTimeOutURL)
     * ResponseParserType.C2B_STK MpesaResponse{ internalStatus=true | false, Body }
     * ResponseParserType.B2B_PAYMENT MpesaResponse{ internalStatus=true | false, Result }
     * ResponseParserType.B2B_STK MpesaResponse{ internalStatus=true | false, resultCode, resultDesc,requestId, amount, paymentReference, resultType, conversationId, transactionId, status }
     * ResponseParserType.B2C MpesaResponse{ internalStatus=true | false, result }
     *
     * @param mpesaResponse
     * @param responseParserType
     */
    public void responseParser(@NotNull MpesaResponse mpesaResponse, @NotNull ResponseParserType responseParserType) {
        switch (responseParserType) {
            case ResponseParserType.C2B_STK -> {
                mpesaResponse.setInternalStatus(mpesaResponse.getBody().getStkCallback().getResultCode() == 0);
                //extract amount, mpesaReference;
                if (mpesaResponse.getBody().getStkCallback().getCallbackMetadata() != null
                        && mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems() != null
                        && !mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().isEmpty()) {

                    for (Item item : mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems()) {
                        if (item.getName().equals("Amount")) {
                            mpesaResponse.setAmount(Double.parseDouble(item.getValue()));
                        }
                        if (item.getName().equals("MpesaReceiptNumber")) {
                            mpesaResponse.setMpesaReference(item.getValue());
                        }
                        if (item.getName().equals("PhoneNumber")) {
                            mpesaResponse.setPhoneNumber(item.getValue());
                        }
                        if (item.getName().equals("TransactionDate")) {
                            try {
                                mpesaResponse.setTransactionDate(Helpers.formatDateTimeToInstant(item.getValue()));
                            } catch (Exception e) {
                            }
                        }

                    }
                }
            }
            case ResponseParserType.B2B_PAYMENT, B2C, TAX_REMITTANCE ->
                    mpesaResponse.setInternalStatus(mpesaResponse.getResult().getResultCode() == 0);
            case ResponseParserType.B2B_STK ->
                    mpesaResponse.setInternalStatus(mpesaResponse.getResultCode().equals("0"));
            case ResponseParserType.C2B_TRANSACTION_STATUS -> {
                mpesaResponse.setInternalStatus(mpesaResponse.getResult().getResultCode() == 0);
                if (mpesaResponse.getResult().getResultParameters() != null
                        && mpesaResponse.getResult().getResultParameters().getResultParameter() != null
                        && !mpesaResponse.getResult().getResultParameters().getResultParameter().isEmpty()) {

                    //extract mpesa amount, mpesaReference
                    for (ResultParameter resultParameter : mpesaResponse.getResult().getResultParameters().getResultParameter()) {
                        if (resultParameter.getKey().equals("Amount")) {
                            mpesaResponse.setAmount(Double.parseDouble(resultParameter.getValue()));
                        }
                        if (resultParameter.getKey().equals("ReceiptNo")) {
                            mpesaResponse.setMpesaReference(resultParameter.getValue());
                        }
                        if (resultParameter.getKey().equals("DebitPartyName") && !resultParameter.getValue().isBlank()) {
                            String[] debitPartyName = Arrays.stream(resultParameter.getValue().split("-"))
                                    .map(String::trim)
                                    .filter(s -> !s.isBlank())
                                    .toArray(String[]::new);
                            int debitPartyNameLength = debitPartyName.length;
                            if (debitPartyNameLength > 0) mpesaResponse.setPhoneNumber(debitPartyName[0]);
                            if (debitPartyNameLength > 1) mpesaResponse.setFullNames(debitPartyName[1]);
                        }
                        if (resultParameter.getKey().equals("FinalisedTime")) {
                            try {
                                mpesaResponse.setTransactionDate(Helpers.formatDateTimeToInstant(resultParameter.getValue()));
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the status of an  STK push
     *
     * @return MpesaResponse { internalStatus=true|false, responseCode, responseDescription, merchantRequestID, checkoutRequestID, resultCode, resultDesc }
     * @throws Exception
     */
    public MpesaResponse stkQuery() throws Exception {
        MpesaRequest mpesaRequest = initialStkSetup(MpesaRequestType.STK_QUERY);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.STK_QUERY)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            String resultCode = mpesaResponse.getResultCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0 && resultCode != null && resultCode.equals("0"));
            return mpesaResponse;
        }
    }

    /**
     * @return MpesaResponse { internalStatus=true|false, responseCode, originatorCoversationID, responseDescription }
     * @throws Exception
     */
    public MpesaResponse C2BRegisterURL() throws Exception {
        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.C2B_REGISTER_URL);
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.C2B_REGISTER_URL)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

    /**
     * @return MpesaResponse { internalStatus=true|false, originatorConversationId, conversationId, responseCode, responseDescription }
     * @throws Exception
     */
    public MpesaResponse C2BTransactionStatus() throws Exception {
        //generate security credentials
        String securityCredentials = Helpers.generateSecurityCredentials(initiatorPassword, environment == Environment.DEVELOPMENT ? Helpers.MPESA_CERT_DEV : Helpers.MPESA_CERT_PROD);
        mpesaRequestDto.setSecurityCredential(securityCredentials);
        mpesaRequestDto.setInitiator(initiatorName);

        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.C2B_TRANSACTION_STATUS);
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.C2B_TRANSACTION_STATUS_URL)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

    /**
     * Generates validation response
     *
     * @param responseStatus
     * @param resultDesc
     * @param thirdPartyTransID
     * @return MpesaResponse { resultCode, resultDesc, thirdPartyTransID(optional) }
     */
    public MpesaResponse generateValidationResponse(@NotNull ResponseStatus responseStatus, @NotNull ResultDesc resultDesc, String thirdPartyTransID) {
        MpesaResponse mpesaResponse = new MpesaResponse();
        mpesaResponse.setResultCode(responseStatus.getValue());
        mpesaResponse.setResultDesc(resultDesc.getValue());
        if (thirdPartyTransID != null && !thirdPartyTransID.isEmpty()) {
            mpesaResponse.setThirdPartyTransID(thirdPartyTransID);
        }
        return mpesaResponse;
    }

    /**
     * Generates acknowledgment response
     *
     * @return MpesaResponse { resultCode, resultDesc }
     */
    public MpesaResponse generateAcknowledgmentResponse() {
        MpesaResponse mpesaResponse = new MpesaResponse();
        mpesaResponse.setResultCode(ResponseStatus.SUCCESS.getValue());
        mpesaResponse.setResultDesc(ResultDesc.SUCCESS.getValue());
        return mpesaResponse;
    }


    /**
     * Make payment from paybill or buy goods to paybill or buy goods account
     *
     * @return MpesaResponse Success{ internalStatus=true, responseCode, originatorConversationId, conversationId, responseDescription  } ||
     * MpesaResponse Failed{ internalStatus=false, requestId, errorCode, errorMessage }
     * @throws Exception
     */
    public MpesaResponse B2BPayment() throws Exception {
        B2BPaymentValidation();
        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);

        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.B2B_PAYMENT)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }


    /**
     * Make payment from paybill or buy goods to paybill or buy goods account using USSDPUSH
     *
     * @return MpesaResponse Success{ internalStatus=true, ResponseBody  } ||
     * MpesaResponse Failed{ internalStatus=false, errorCode, errorMessage, ResponseBody }
     * @throws Exception
     */
    public MpesaResponse B2BStk() throws Exception {
        if (mpesaRequestDto.getAmt() < MIN_B2B_AMOUNT) {
            throw new RuntimeException("Amt should be greater or equal to " + MIN_B2B_AMOUNT);
        }

        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.B2B_STK);
        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);

        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.B2B_STK)) {
            MpesaResponse mpesaResponse = build.post();

            ResponseBody responseBody = mpesaResponse.getResponseBody();
            Integer code = responseBody != null ? responseBody.getCode() : null;
            String status = responseBody != null ? responseBody.getStatus() : null;
            mpesaResponse.setInternalStatus(code != null && code == 0);

            if (!mpesaResponse.isInternalStatus() && code != null) {
                mpesaResponse.setErrorCode(code.toString());
            }
            if (!mpesaResponse.isInternalStatus() && status != null) {
                mpesaResponse.setErrorMessage(status);
            }
            return mpesaResponse;
        }
    }

    /**
     * Disburse cash from business to customer
     *
     * @return MpesaResponse Success{ internalStatus=true, responseCode, originatorConversationId, conversationId, responseDescription  } ||
     * MpesaResponse Failed{ internalStatus=false, errorCode, errorMessage, requestId }
     * @throws Exception
     */
    public MpesaResponse B2CDisbursement() throws Exception {
        //generate security credentials
        String securityCredentials = Helpers.generateSecurityCredentials(initiatorPassword, environment == Environment.DEVELOPMENT ? Helpers.MPESA_CERT_DEV : Helpers.MPESA_CERT_PROD);
        mpesaRequestDto.setSecurityCredential(securityCredentials);
        mpesaRequestDto.setInitiatorName(initiatorName);

        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.B2C);
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.B2C)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

    /**
     * Remit tax to tax organization
     *
     * @return MpesaResponse Success{ internalStatus=true, responseCode, originatorConversationId, conversationId, responseDescription  } ||
     * MpesaResponse Failed{ internalStatus=false, errorCode, errorMessage, requestId }
     * @throws Exception
     */
    public MpesaResponse remitTax() throws Exception {
        //generate security credentials
        String securityCredentials = Helpers.generateSecurityCredentials(initiatorPassword, environment == Environment.DEVELOPMENT ? Helpers.MPESA_CERT_DEV : Helpers.MPESA_CERT_PROD);
        mpesaRequestDto.setSecurityCredential(securityCredentials);
        mpesaRequestDto.setInitiator(initiatorName);
        mpesaRequestDto.setCommandId(TAX_COMMAND_ID);
        mpesaRequestDto.setSenderIdentifierType(SENDER_IDENTIFIER_TYPE);
        mpesaRequestDto.setRecieverIdentifierType(RECEIVER_IDENTIFIER_TYPE);
        mpesaRequestDto.setPartyB(TAX_ORG_BUSINESS_SHORTCODE);

        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.TAX_REMITTANCE);
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.TAX_REMITTANCE)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

    /**
     * Generate dynamic QR Code
     *
     * @return MpesaResponse Success{ internalStatus=true, responseCode, requestID, responseDescription, qrCode  } ||
     * MpesaResponse Failed{ internalStatus=false, errorCode, errorMessage, requestId }
     * @throws Exception
     */
    public MpesaResponse generateDynamicQrCode() throws Exception {
        MpesaRequest mpesaRequest = new MpesaRequest();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.DYNAMIC_QR);
        mpesaRequest.setMpesaRequestDto(mpesaRequestDto);
        try (ApiClient<MpesaRequest, MpesaResponse> apiClient = new ApiClient<>();
             ApiClient<MpesaRequest, MpesaResponse> build = buildRequest(apiClient, mpesaRequest, MpesaURL.DYNAMIC_QR)) {
            MpesaResponse mpesaResponse = build.post();
            Integer responseCode = mpesaResponse.getResponseCode();
            mpesaResponse.setInternalStatus(responseCode != null && responseCode == 0);
            return mpesaResponse;
        }
    }

}

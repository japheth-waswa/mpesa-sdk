package mpesa;

import base.Helpers;
import com.fasterxml.jackson.core.JsonProcessingException;
import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MpesaClientTest {

    private final String CONSUMER_KEY = "";
    private final String CONSUMER_SECRET = "";
    private final String MPESA_PASS_KEY = "";
    private final String INITIATOR_NAME = "";
    private final String INITIATOR_PASSWORD = "";
    private final int BUSINESS_SHORT_CODE = 174379;
    private final int SENDING_SHORT_CODE = 7318002;
    private final int BUSINESS_SHORT_CODE_B2C = 600995;
    private final String TAX_PRN="353353";
    private final long PARTY_A = 600981;
    private final int PARTY_B = 600000;
    private final long PHONE_NUMBER = 0L;
    private final long PHONE_NUMBER_B2B = 0L;
    private final String WEB_HOOK_BASE_URL = "";

    private final String CALLBACK_URL = WEB_HOOK_BASE_URL + "/hook";
    private final String VALIDATION_URL = WEB_HOOK_BASE_URL + "/validation";
    private final String CONFIRMATION_URL = WEB_HOOK_BASE_URL + "/confirmation";
    private final String TIMEOUT_URL = WEB_HOOK_BASE_URL + "/hook";
    private final String RESULT_URL = WEB_HOOK_BASE_URL + "/hook";

    private final String CHECKOUT_REQUEST_ID = "ws_CO_21022024131447745719726698";

    private String generateRandomStr() {
        String str = UUID.randomUUID().toString();
        return str.substring(0, 8);
    }

    @Test
    void generateAccessToken() {
        try {
            MpesaResponse mpesaResponse = new MpesaClient()
                    .environment(Environment.DEVELOPMENT)
                    .consumerKey(CONSUMER_KEY)
                    .consumerSecret(CONSUMER_SECRET)
                    .generateAccessToken();
            System.out.println(mpesaResponse);
            assertNotNull(mpesaResponse);
            assertNotNull(mpesaResponse.getAccessToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //
    @Test
    void sendStk_success() throws Exception {
        MpesaRequestDto mpesaRequest = new MpesaRequestDto();
        mpesaRequest.setStkTransactionType(STKTransactionType.PAY_BILL);
        mpesaRequest.setBusinessShortCode(BUSINESS_SHORT_CODE);
        mpesaRequest.setAmount(1);
        mpesaRequest.setPhoneNumber(PHONE_NUMBER);
        mpesaRequest.setCallbackURL(CALLBACK_URL);
        mpesaRequest.setAccountReference(generateRandomStr());
        mpesaRequest.setTransactionDesc("Some random description  = " + mpesaRequest.getAccountReference());

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .passKey(MPESA_PASS_KEY)
                .mpesaRequestDto(mpesaRequest)
                .stkSend();
        System.out.println(mpesaResponse);
        assertNotNull(mpesaResponse);
        assertTrue(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getMerchantRequestID());
        assertNotNull(mpesaResponse.getCheckoutRequestID());
        assertEquals(0, mpesaResponse.getResponseCode());
    }

    @Test
    void sendStk_fail_invalid_callback_url() throws Exception {
        MpesaRequestDto mpesaRequest = new MpesaRequestDto();
        mpesaRequest.setStkTransactionType(STKTransactionType.PAY_BILL);
        mpesaRequest.setBusinessShortCode(BUSINESS_SHORT_CODE);
        mpesaRequest.setAmount(5);
        mpesaRequest.setPhoneNumber(PHONE_NUMBER);
        mpesaRequest.setCallbackURL("");
        mpesaRequest.setAccountReference(generateRandomStr());
        mpesaRequest.setTransactionDesc("Some random description  = " + mpesaRequest.getAccountReference());

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .passKey(MPESA_PASS_KEY)
                .mpesaRequestDto(mpesaRequest)
                .stkSend();
        System.out.println(mpesaResponse);
        assertNotNull(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getErrorCode());
    }

    @Test
    void stkCallback_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Body":{"stkCallback":{"MerchantRequestID":"29115-34620561-1","CheckoutRequestID":"ws_CO_191220191020363925","ResultCode":0,"ResultDesc":"The service request is processed successfully.","CallbackMetadata":{"Item":[{"Name":"Amount","Value":1},{"Name":"MpesaReceiptNumber","Value":"NLJ7RT61SV"},{"Name":"TransactionDate","Value":20191219102115},{"Name":"PhoneNumber","Value":254708374149}]}}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.C2B_STK);
        assertTrue(mpesaResponse.isInternalStatus());
    }

    @Test
    void stkCallback_fail() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Body":{"stkCallback":{"MerchantRequestID":"6e86-45dd-91ac-fd5d4178ab52485825","CheckoutRequestID":"ws_CO_20022024202003133719726698","ResultCode":1037,"ResultDesc":"DS timeout user cannot be reached"}}}                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.C2B_STK);
        assertFalse(mpesaResponse.isInternalStatus());
    }

    @Test
    void stkQuery() throws Exception {
        MpesaRequestDto mpesaRequest = new MpesaRequestDto();
        mpesaRequest.setBusinessShortCode(BUSINESS_SHORT_CODE);
        mpesaRequest.setCheckoutRequestID(CHECKOUT_REQUEST_ID);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .passKey(MPESA_PASS_KEY)
                .mpesaRequestDto(mpesaRequest)
                .stkQuery();
        System.out.println(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
        assertEquals(CHECKOUT_REQUEST_ID, mpesaResponse.getCheckoutRequestID());
    }

    @Test
    void C2BRegisterURL() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setBusinessShortCode(BUSINESS_SHORT_CODE);
        mpesaRequestDto.setRegisterURLResponseType(RegisterURLResponseType.CANCELLED);
        mpesaRequestDto.setConfirmationURL(CONFIRMATION_URL);
        mpesaRequestDto.setValidationURL(VALIDATION_URL);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .mpesaRequestDto(mpesaRequestDto)
                .C2BRegisterURL();
        System.out.println(mpesaResponse);
        assertTrue(mpesaResponse.isInternalStatus());
        assertEquals(0, mpesaResponse.getResponseCode());
    }

    @Test
    void generateValidationResponse_success() {
        MpesaResponse mpesaResponse = new MpesaClient()
                .generateValidationResponse(ResponseStatus.SUCCESS, ResultDesc.ACCEPTED, null);
        System.out.println(mpesaResponse);
        assertEquals(ResponseStatus.SUCCESS.getValue(), mpesaResponse.getResultCode());
        assertEquals(ResultDesc.ACCEPTED.getValue(), mpesaResponse.getResultDesc());
    }

    @Test
    void parseValidationConfirmationRequestBody() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"TransactionType":"Pay Bill","TransID":"RKTQDM7W6S","TransTime":"20191122063845","TransAmount":"10","BusinessShortCode":"600638","BillRefNumber":"invoice008","InvoiceNumber":"","OrgAccountBalance":"","ThirdPartyTransID":"","MSISDN":"25470****149","FirstName":"John","MiddleName":"","LastName":"Doe"}
                """);
        System.out.println(mpesaResponse);
        System.out.println(mpesaResponse.getTransTime());
        assertEquals("2019-11-22T06:38:45", mpesaResponse.getTransTime().toString());
    }

    @Test
    void generateValidationResponse_fail() {
        MpesaResponse mpesaResponse = new MpesaClient()
                .generateValidationResponse(ResponseStatus.INVALID_AMOUNT, ResultDesc.REJECTED, "XYZ123");
        System.out.println(mpesaResponse);
        assertEquals(ResponseStatus.INVALID_AMOUNT.getValue(), mpesaResponse.getResultCode());
        assertEquals(ResultDesc.REJECTED.getValue(), mpesaResponse.getResultDesc());
        assertEquals("XYZ123", mpesaResponse.getThirdPartyTransID());
    }

    @Test
    void generateAcknowledgmentResponse() {
        MpesaResponse mpesaResponse = new MpesaClient()
                .generateAcknowledgmentResponse();
        System.out.println(mpesaResponse);
        assertEquals(ResponseStatus.SUCCESS.getValue(), mpesaResponse.getResultCode());
        assertEquals(ResultDesc.SUCCESS.getValue(), mpesaResponse.getResultDesc());
    }

    @Test
    void B2BPayment_success() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.B2B_PAY_BILL); //MpesaRequestType.B2B_BUY_GOODS | MpesaRequestType.B2B_PAY_BILL
        mpesaRequestDto.setAmount(2);
        mpesaRequestDto.setPartyA(PARTY_A);
        mpesaRequestDto.setPartyB((long) PARTY_B);
        mpesaRequestDto.setAccountReference(generateRandomStr());
        mpesaRequestDto.setPhoneNumber(PHONE_NUMBER_B2B);
        mpesaRequestDto.setRemarks("some random data");
        mpesaRequestDto.setQueueTimeOutURL(TIMEOUT_URL);
        mpesaRequestDto.setResultURL(RESULT_URL);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .initiatorName(INITIATOR_NAME)
                .initiatorPassword(INITIATOR_PASSWORD)
                .mpesaRequestDto(mpesaRequestDto)
                .B2BPayment();
        System.out.println(mpesaResponse);
        assertNotNull(mpesaResponse);
        assertTrue(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getOriginatorConversationId());
        assertNotNull(mpesaResponse.getOriginatorConversationId());
        assertNotNull(mpesaResponse.getResponseCode());
        assertNotNull(mpesaResponse.getResponseDescription());
    }

    @Test
    void B2BPayment_fail() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setMpesaRequestType(MpesaRequestType.B2B_PAY_BILL); //MpesaRequestType.B2B_BUY_GOODS | MpesaRequestType.B2B_PAY_BILL
        mpesaRequestDto.setAmount(2);
        mpesaRequestDto.setPartyA(PARTY_A);
        mpesaRequestDto.setPartyB((long) PARTY_B);
        mpesaRequestDto.setAccountReference(generateRandomStr());
        mpesaRequestDto.setPhoneNumber(PHONE_NUMBER_B2B);
        mpesaRequestDto.setRemarks("some random data");
        mpesaRequestDto.setQueueTimeOutURL(TIMEOUT_URL);
//        mpesaRequestDto.setResultURL(RESULT_URL);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .initiatorName(INITIATOR_NAME)
                .initiatorPassword(INITIATOR_PASSWORD)
                .mpesaRequestDto(mpesaRequestDto)
                .B2BPayment();
        System.out.println(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getErrorCode());
    }

    @Test
    void B2BPayment_response_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":"0","ResultCode":"0","ResultDesc":"The service request is processed successfully","OriginatorConversationID":"626f6ddf-ab37-4650-b882-b1de92ec9aa4","ConversationID":"12345677dfdf89099B3","TransactionID":"QKA81LK5CY","ResultParameters":{"ResultParameter":[{"Key":"DebitAccountBalance","Value":"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}"},{"Key":"Amount","Value":"190.00"},{"Key":"DebitPartyAffectedAccountBalance","Value":"Working Account|KES|346568.83|6186.83|340382.00|0.00"},{"Key":"TransCompletedTime","Value":"20221110110717"},{"Key":"DebitPartyCharges","Value":""},{"Key":"ReceiverPartyPublicName","Value":"000000– Biller Companty"},{"Key":"Currency","Value":"KES"},{"Key":"InitiatorAccountCurrentBalance","Value":"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}"}]},"ReferenceData":{"ReferenceItem":[{"Key":"BillReferenceNumber","Value":"19008"},{"Key":"QueueTimeoutURL","Value":"https://mydomain.com/b2b/businessbuygoods/queue/"}]}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2B_PAYMENT);
        System.out.println(mpesaResponse);

        assertTrue(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("QKA81LK5CY", mpesaResponse.getResult().getTransactionId());
        assertEquals("190.00", mpesaResponse.getResult().getResultParameters().getResultParameter().get(1).getValue());
        assertEquals("BillReferenceNumber", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(0).getKey());
    }

    @Test
    void B2BPayment_response_failed() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":0,"ResultCode":17,"ResultDesc":"System internal error.","OriginatorConversationID":"c0d2-4b9a-a71a-12bae346ef6e226156","ConversationID":"AG_20240226_20105cd4867a695abaef","TransactionID":"0000000000000000","ReferenceData":{"ReferenceItem":[{"Key":"BillReferenceNumber","Value":"1d2ea2b9"},{"Key":"QueueTimeoutURL","Value":"https://internalsandbox.safaricom.co.ke/mpesa/b2bresults/v1/submit"},{"Key":"Occassion"}]}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2B_PAYMENT);
        System.out.println(mpesaResponse);

        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("https://internalsandbox.safaricom.co.ke/mpesa/b2bresults/v1/submit", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(1).getValue());
    }

    @Test
    void b2BStk_success() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setSendingPartyShortCode(SENDING_SHORT_CODE);
        mpesaRequestDto.setReceivingPartyShortCode(BUSINESS_SHORT_CODE);
        mpesaRequestDto.setReceivingPartyName("Test"); //AlphaNumeric
        mpesaRequestDto.setAmt(10); //Min 10
        mpesaRequestDto.setPaymentRef("Y" + generateRandomStr()); //AlphaNumeric
        mpesaRequestDto.setCallback(WEB_HOOK_BASE_URL);
        mpesaRequestDto.setRequestRefId(UUID.randomUUID().toString());

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .mpesaRequestDto(mpesaRequestDto)
                .B2BStk();
        System.out.println(mpesaResponse);
        assertTrue(mpesaResponse.isInternalStatus());
    }

    @Test
    void b2BStk_fail() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setSendingPartyShortCode(SENDING_SHORT_CODE);
        mpesaRequestDto.setReceivingPartyShortCode(BUSINESS_SHORT_CODE);
        mpesaRequestDto.setReceivingPartyName("Test"); //AlphaNumeric
        mpesaRequestDto.setAmt(10); //Min 10
//        mpesaRequestDto.setPaymentRef("Y" + generateRandomStr()); //AlphaNumeric
        mpesaRequestDto.setCallback(WEB_HOOK_BASE_URL);
        mpesaRequestDto.setRequestRefId(UUID.randomUUID().toString());

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .mpesaRequestDto(mpesaRequestDto)
                .B2BStk();
        System.out.println(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getErrorCode());
        assertNotNull(mpesaResponse.getErrorMessage());
    }

    @Test
    void B2BStk_response_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"resultCode":"0","resultDesc":"The service request is processed successfully.","amount":"71.0","requestId":"404e1aec-19e0-4ce3-973d-bd92e94c8021","resultType":"0","conversationID":"AG_20230426_2010434680d9f5a73766","transactionId":"RDQ01NFT1Q","status":"SUCCESS"}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2B_STK);
        System.out.println(mpesaResponse);

        assertTrue(mpesaResponse.isInternalStatus());
        assertEquals("0", mpesaResponse.getResultCode());
        assertEquals("404e1aec-19e0-4ce3-973d-bd92e94c8021", mpesaResponse.getRequestId());
        assertEquals(71.0, mpesaResponse.getAmount());
    }

    @Test
    void B2BStk_response_failed() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"resultCode":"4001","resultDesc":"User cancelled transaction","requestId":"c2a9ba32-9e11-4b90-892c-7bc54944609a","amount":"71.0","paymentReference":"MAndbubry3hi"}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2B_STK);
        System.out.println(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
        assertEquals("MAndbubry3hi", mpesaResponse.getPaymentReference());
    }

    @Test
    void b2CDisbursement() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setOriginatorConversationId(UUID.randomUUID().toString());
        mpesaRequestDto.setB2CCommandID(B2CCommandID.SALARY_PAYMENT);
        mpesaRequestDto.setAmount(10);
        mpesaRequestDto.setBusinessShortCode(BUSINESS_SHORT_CODE_B2C);
        mpesaRequestDto.setPhoneNumber(PHONE_NUMBER_B2B);
        mpesaRequestDto.setRemarks("testing 123");
        mpesaRequestDto.setQueueTimeOutURL(TIMEOUT_URL);
        mpesaRequestDto.setResultURL(RESULT_URL);
        mpesaRequestDto.setOccassion("holidayguarantee");//Alpha-numeric (Max 100 chars)

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .initiatorName(INITIATOR_NAME)
                .initiatorPassword(INITIATOR_PASSWORD)
                .mpesaRequestDto(mpesaRequestDto)
                .B2CDisbursement();
        System.out.println(mpesaResponse);
        assertTrue(mpesaResponse.isInternalStatus());
        assertEquals(0, mpesaResponse.getResponseCode());
    }

    @Test
    void B2CPayment_response_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":0,"ResultCode":0,"ResultDesc":"The service request is processed successfully.","OriginatorConversationID":"10571-7910404-1","ConversationID":"AG_20191219_00004e48cf7e3533f581","TransactionID":"NLJ41HAY6Q","ResultParameters":{"ResultParameter":[{"Key":"TransactionAmount","Value":10},{"Key":"TransactionReceipt","Value":"NLJ41HAY6Q"},{"Key":"B2CRecipientIsRegisteredCustomer","Value":"Y"},{"Key":"B2CChargesPaidAccountAvailableFunds","Value":-4510},{"Key":"ReceiverPartyPublicName","Value":"254708374149 - John Doe"},{"Key":"TransactionCompletedDateTime","Value":"19.12.2019 11:45:50"},{"Key":"B2CUtilityAccountAvailableFunds","Value":10116},{"Key":"B2CWorkingAccountAvailableFunds","Value":900000}]},"ReferenceData":{"ReferenceItem":{"Key":"QueueTimeoutURL","Value":"https://internalsandbox.safaricom.co.ke/mpesa/b2cresults/v1/submit"}}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2C);
        System.out.println(mpesaResponse);

        assertTrue(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("NLJ41HAY6Q", mpesaResponse.getResult().getTransactionId());
        assertEquals("https://internalsandbox.safaricom.co.ke/mpesa/b2cresults/v1/submit", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(0).getValue());
        assertEquals("B2CChargesPaidAccountAvailableFunds", mpesaResponse.getResult().getResultParameters().getResultParameter().get(3).getKey());
    }

    @Test
    void B2CPayment_response_failed() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":0,"ResultCode":2001,"ResultDesc":"The initiator information is invalid.","OriginatorConversationID":"9378b6f5-6d44-43a1-8c36-53fddb8414c0","ConversationID":"AG_20240226_201039308b470bd2323f","TransactionID":"SBQ72IFPXT","ReferenceData":{"ReferenceItem":{"Key":"QueueTimeoutURL","Value":"https://internalsandbox.safaricom.co.ke/mpesa/b2cresults/v1/submit"}}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.B2C);
        System.out.println(mpesaResponse);

        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("AG_20240226_201039308b470bd2323f", mpesaResponse.getResult().getConversationId());
        assertEquals("QueueTimeoutURL", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(0).getKey());
    }

    @Test
    void remitTax() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setTaxPRN(TAX_PRN);
        mpesaRequestDto.setBusinessShortCode((int)PARTY_A);
        mpesaRequestDto.setAmount(239);
        mpesaRequestDto.setRemarks("Testing");//max 100 chars
        mpesaRequestDto.setQueueTimeOutURL(TIMEOUT_URL);
        mpesaRequestDto.setResultURL(RESULT_URL);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .initiatorName(INITIATOR_NAME)
                .initiatorPassword(INITIATOR_PASSWORD)
                .mpesaRequestDto(mpesaRequestDto)
                .remitTax();
        System.out.println(mpesaResponse);
    }

    @Test
    void remitTax_response_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":"0","ResultCode":"0","ResultDesc":"The service request is processed successfully","OriginatorConversationID":"626f6ddf-ab37-4650-b882-b1de92ec9aa4","ConversationID":"AG_20181005_00004d7ee675c0c7ee0b","TransactionID":"QKA81LK5CY","ResultParameters":{"ResultParameter":[{"Key":"DebitAccountBalance","Value":"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}"},{"Key":"Amount","Value":"190.00"},{"Key":"DebitPartyAffectedAccountBalance","Value":"Working Account|KES|346568.83|6186.83|340382.00|0.00"},{"Key":"TransCompletedTime","Value":"20221110110717"},{"Key":"DebitPartyCharges","Value":""},{"Key":"ReceiverPartyPublicName","Value":"00000 - Tax Collecting Company"},{"Key":"Currency","Value":"KES"},{"Key":"InitiatorAccountCurrentBalance","Value":"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}"}]},"ReferenceData":{"ReferenceItem":[{"Key":"BillReferenceNumber","Value":"19008"},{"Key":"QueueTimeoutURL","Value":"https://mydomain.com/b2b/remittax/queue/"}]}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.TAX_REMITTANCE);
        System.out.println(mpesaResponse);

        assertTrue(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("QKA81LK5CY", mpesaResponse.getResult().getTransactionId());
        assertEquals("DebitPartyCharges", mpesaResponse.getResult().getResultParameters().getResultParameter().get(4).getKey());
        assertEquals("19008", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(0).getValue());
    }

    @Test
    void remitTax_response_failed() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Result":{"ResultType":0,"ResultCode":2001,"ResultDesc":"The initiator information is invalid.","OriginatorConversationID":"c0d2-4b9a-a71a-12bae346ef6e242067","ConversationID":"AG_20240227_20103b25abc12157c81a","TransactionID":"SBR12IFWUT","ReferenceData":{"ReferenceItem":[{"Key":"BillReferenceNumber","Value":353353},{"Key":"QueueTimeoutURL","Value":"https://internalsandbox.safaricom.co.ke/mpesa/b2bresults/v1/submit"},{"Key":"Occassion"}]}}}
                """);
        new MpesaClient()
                .responseParser(mpesaResponse, ResponseParserType.TAX_REMITTANCE);
        System.out.println(mpesaResponse);

        assertFalse(mpesaResponse.isInternalStatus());
        assertNotNull(mpesaResponse.getResult());
        assertEquals("c0d2-4b9a-a71a-12bae346ef6e242067", mpesaResponse.getResult().getOriginatorConversationId());
        assertEquals("Occassion", mpesaResponse.getResult().getReferenceData().getReferenceItem().get(2).getKey());
    }


    @Test
    void generateDynamicQrCode() throws Exception {
        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setAmount(239);
        mpesaRequestDto.setMerchantName("TEST SUPERMARKET");
        mpesaRequestDto.setRefNo(generateRandomStr());

        //if trxCodeType not among SEND_MONEY_MOBILE_NUMBER,WITHDRAW_CASH_AGENT_TILL then setBusinessShortCode
        mpesaRequestDto.setTrxCodeType(TrxCodeType.BUY_GOODS);
        mpesaRequestDto.setBusinessShortCode(373132);

        ////if trxCodeType IS WITHDRAW_CASH_AGENT_TILL then setAgentTill
        //mpesaRequestDto.setTrxCodeType(TrxCodeType.BUY_GOODS);
        //mpesaRequestDto.setAgentTill(373132);

        ////if trxCodeType is SEND_MONEY_MOBILE_NUMBER then setPhoneNumber
        //mpesaRequestDto.setTrxCodeType(TrxCodeType.BUY_GOODS);
        //mpesaRequestDto.setPhoneNumber(PHONE_NUMBER_B2B);

        MpesaResponse mpesaResponse = new MpesaClient()
                .environment(Environment.DEVELOPMENT)
                .consumerSecret(CONSUMER_SECRET)
                .consumerKey(CONSUMER_KEY)
                .mpesaRequestDto(mpesaRequestDto)
                .generateDynamicQrCode();
        System.out.println(mpesaResponse);
        assertFalse(mpesaResponse.isInternalStatus());
    }

}
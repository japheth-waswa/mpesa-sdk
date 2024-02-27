package mpesa;

import base.Request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import mpesa.dto.MpesaRequestDto;
import mpesa.util.CommandID;
import mpesa.util.MpesaRequestType;
import mpesa.util.TrxCodeType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaRequest implements Request {

    private MpesaRequestDto mpesaRequestDto;

    private MpesaRequestDto stkSendRequestPayload() {
        MpesaRequestDto stkRequest = new MpesaRequestDto();
        stkRequest.setBusinessShortCode(mpesaRequestDto.getBusinessShortCode());
        stkRequest.setPassword(mpesaRequestDto.getPassword());
        stkRequest.setTimestamp(mpesaRequestDto.getTimestamp());
        stkRequest.setTransactionType(mpesaRequestDto.getStkTransactionType().getValue());
        stkRequest.setAmount(mpesaRequestDto.getAmount());
        stkRequest.setPartyA(mpesaRequestDto.getPhoneNumber());
        stkRequest.setPartyB(Long.valueOf(mpesaRequestDto.getBusinessShortCode()));
        stkRequest.setPhoneNumber(mpesaRequestDto.getPhoneNumber());
        stkRequest.setCallbackURL(mpesaRequestDto.getCallbackURL());
        stkRequest.setAccountReference(mpesaRequestDto.getAccountReference());
        stkRequest.setTransactionDesc(mpesaRequestDto.getTransactionDesc());
        return stkRequest;
    }

    private MpesaRequestDto stkQueryRequestPayload() {
        MpesaRequestDto stkRequest = new MpesaRequestDto();
        stkRequest.setBusinessShortCode(mpesaRequestDto.getBusinessShortCode());
        stkRequest.setPassword(mpesaRequestDto.getPassword());
        stkRequest.setTimestamp(mpesaRequestDto.getTimestamp());
        stkRequest.setCheckoutRequestID(mpesaRequestDto.getCheckoutRequestID());
        return stkRequest;
    }

    private MpesaRequestDto C2BRegisterURLPayload() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();
        requestPayload.setShortCode(mpesaRequestDto.getBusinessShortCode());
        requestPayload.setResponseType(mpesaRequestDto.getRegisterURLResponseType().getValue());
        requestPayload.setConfirmationURL(mpesaRequestDto.getConfirmationURL());
        requestPayload.setValidationURL(mpesaRequestDto.getValidationURL());
        return requestPayload;
    }

    private MpesaRequestDto B2BPaymentRequest() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();
        if (mpesaRequestDto.getMpesaRequestType() == MpesaRequestType.B2B_PAY_BILL) {
            requestPayload.setCommandId(CommandID.PAY_BILL.getValue());
        } else {
            requestPayload.setCommandId(CommandID.BUY_GOODS.getValue());
        }
        requestPayload.setInitiator(mpesaRequestDto.getInitiator());
        requestPayload.setSecurityCredential(mpesaRequestDto.getSecurityCredential());
        requestPayload.setSenderIdentifierType(mpesaRequestDto.getSenderIdentifierType());
        requestPayload.setRecieverIdentifierType(mpesaRequestDto.getRecieverIdentifierType());
        requestPayload.setAmount(mpesaRequestDto.getAmount());
        requestPayload.setPartyA(mpesaRequestDto.getPartyA());
        requestPayload.setPartyB(mpesaRequestDto.getPartyB());
        requestPayload.setAccountReference(mpesaRequestDto.getAccountReference());
        requestPayload.setRequester(mpesaRequestDto.getPhoneNumber());
        requestPayload.setRemarks(mpesaRequestDto.getRemarks());
        requestPayload.setQueueTimeOutURL(mpesaRequestDto.getQueueTimeOutURL());
        requestPayload.setResultURL(mpesaRequestDto.getResultURL());
        return requestPayload;
    }

    private MpesaRequestDto B2BStkPayload() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();
        requestPayload.setSendingPartyShortCode(mpesaRequestDto.getSendingPartyShortCode());
        requestPayload.setReceivingPartyShortCode(mpesaRequestDto.getReceivingPartyShortCode());
        requestPayload.setReceivingPartyName(mpesaRequestDto.getReceivingPartyName());
        requestPayload.setAmt(mpesaRequestDto.getAmt());
        requestPayload.setPaymentRef(mpesaRequestDto.getPaymentRef());
        requestPayload.setCallback(mpesaRequestDto.getCallback());
        requestPayload.setRequestRefId(mpesaRequestDto.getRequestRefId());
        return requestPayload;
    }

    private MpesaRequestDto B2CPayload() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();
        requestPayload.setOriginatorConversationId(mpesaRequestDto.getOriginatorConversationId());
        requestPayload.setInitiatorName(mpesaRequestDto.getInitiatorName());
        requestPayload.setSecurityCredential(mpesaRequestDto.getSecurityCredential());
        requestPayload.setCommandId(mpesaRequestDto.getB2CCommandID().getValue());
        requestPayload.setAmount(mpesaRequestDto.getAmount());
        requestPayload.setPartyA(Long.valueOf(mpesaRequestDto.getBusinessShortCode()));
        requestPayload.setPartyB(mpesaRequestDto.getPhoneNumber());
        requestPayload.setRemarks(mpesaRequestDto.getRemarks());
        requestPayload.setQueueTimeOutURL(mpesaRequestDto.getQueueTimeOutURL());
        requestPayload.setResultURL(mpesaRequestDto.getResultURL());
        requestPayload.setOccassion(mpesaRequestDto.getOccassion());
        return requestPayload;
    }

    private MpesaRequestDto TaxRemittancePayload() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();
        requestPayload.setInitiator(mpesaRequestDto.getInitiator());
        requestPayload.setSecurityCredential(mpesaRequestDto.getSecurityCredential());
        requestPayload.setCommandId(mpesaRequestDto.getCommandId());
        requestPayload.setSenderIdentifierType(mpesaRequestDto.getSenderIdentifierType());
        requestPayload.setRecieverIdentifierType(mpesaRequestDto.getRecieverIdentifierType());
        requestPayload.setAmount(mpesaRequestDto.getAmount());
        requestPayload.setPartyA(Long.valueOf(mpesaRequestDto.getBusinessShortCode()));
        requestPayload.setPartyB(mpesaRequestDto.getPartyB());
        requestPayload.setAccountReference(mpesaRequestDto.getTaxPRN());
        requestPayload.setRemarks(mpesaRequestDto.getRemarks());
        requestPayload.setQueueTimeOutURL(mpesaRequestDto.getQueueTimeOutURL());
        requestPayload.setResultURL(mpesaRequestDto.getResultURL());

        return requestPayload;
    }

    private MpesaRequestDto dynamicQRPayload() {
        MpesaRequestDto requestPayload = new MpesaRequestDto();

        TrxCodeType trxCodeType=mpesaRequestDto.getTrxCodeType();
        Long phoneNumber = mpesaRequestDto.getPhoneNumber();
        Integer businessShortCode = mpesaRequestDto.getBusinessShortCode();
        Integer agentTill = mpesaRequestDto.getAgentTill();

        //check if the necessary data has been provided
        if( trxCodeType== null){
            throw new RuntimeException("trxCodeType must be provided");
        }

        if(trxCodeType == TrxCodeType.SEND_MONEY_MOBILE_NUMBER && phoneNumber != null){
            requestPayload.setCpI(phoneNumber.toString());
        }else if(trxCodeType == TrxCodeType.WITHDRAW_CASH_AGENT_TILL && agentTill != null){
            requestPayload.setCpI(agentTill.toString());
        }else if(businessShortCode != null){
            requestPayload.setCpI(businessShortCode.toString());
        }

        if(requestPayload.getCpI() == null){
            throw new RuntimeException("Please provide phoneNumber or businessShortCode or agentTill depending on the trxCodeType provided");
        }

        requestPayload.setTrxCode(trxCodeType.getValue());
        requestPayload.setAmount(mpesaRequestDto.getAmount());
        requestPayload.setMerchantName(mpesaRequestDto.getMerchantName());
        requestPayload.setRefNo(mpesaRequestDto.getRefNo());
        requestPayload.setSize(mpesaRequestDto.getSize());
        return requestPayload;
    }

    @Override
    public String getPostBody() {
        MpesaRequestDto mpesaReqPayload;
        switch (mpesaRequestDto.getMpesaRequestType()) {
            case MpesaRequestType.STK_SEND -> mpesaReqPayload = stkSendRequestPayload();
            case MpesaRequestType.STK_QUERY -> mpesaReqPayload = stkQueryRequestPayload();
            case MpesaRequestType.C2B_REGISTER_URL -> mpesaReqPayload = C2BRegisterURLPayload();
            case MpesaRequestType.B2B_PAY_BILL, MpesaRequestType.B2B_BUY_GOODS -> mpesaReqPayload = B2BPaymentRequest();
            case MpesaRequestType.B2B_STK -> mpesaReqPayload = B2BStkPayload();
            case MpesaRequestType.B2C -> mpesaReqPayload = B2CPayload();
            case MpesaRequestType.TAX_REMITTANCE -> mpesaReqPayload = TaxRemittancePayload();
            case MpesaRequestType.DYNAMIC_QR -> mpesaReqPayload = dynamicQRPayload();
            default -> mpesaReqPayload = new MpesaRequestDto();
        }

        try {
            return new ObjectMapper().writeValueAsString(mpesaReqPayload);
        } catch (JsonProcessingException e) {
            System.out.println(e);
            return "";
        }
    }
}

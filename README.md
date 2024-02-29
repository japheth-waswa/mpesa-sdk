# JAVA M-Pesa SDK
Integrates with Mpesa daraja api
<br>

### Features

- Mpesa Express - STK Push (C2B)
- Mpesa Express - STK Query :- Query the status of an STK Push
- Register Validation and Confirmation URL (C2B)
- Payment Pay bill and buy goods (B2B)
- Ussd Push Pay bill and buy goods (B2B)
- Disbursement (B2C)
- Tax Remittance
- Dynamic QR Code
<br>

## Requirements

- Setup either developer or live account in [Mpesa Developer](https://developer.safaricom.co.ke) portal
- consumerKey, consumerSecret, passKey, 
- initiatorName (The M-Pesa API operator username) 
- initiatorPassword (The M-Pesa API operator password)

## Installation

### Using Maven
1. Add the dependency:
    ```xml
    <dependency>
        <groupId>io.github.japheth-waswa.mpesa</groupId>
        <artifactId>mpesa-sdk</artifactId>
        <version>1.0.1</version>
    </dependency>
    ```


## Usage

### C2B STK Push prompt (Send a payment prompt on the customer's phone)
Send a payment prompt on the customer's phone (Popularly known as STK Push Prompt) to your customer's M-PESA registered phone number requesting them to enter their M-PESA pin to authorize and complete payment.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.Environment;
import mpesa.util.STKTransactionType;

MpesaRequestDto mpesaRequest = new MpesaRequestDto();
mpesaRequest.setStkTransactionType("<REPLACE>");
mpesaRequest.setBusinessShortCode("<REPLACE>");
mpesaRequest.setAmount("<REPLACE>");
mpesaRequest.setPhoneNumber("<REPLACE>");
mpesaRequest.setCallbackURL("<REPLACE>");
mpesaRequest.setAccountReference("<REPLACE>");
mpesaRequest.setTransactionDesc("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.passKey("<REPLACE>")
.mpesaRequestDto(mpesaRequest)
.stkSend();
```

#### MpesaResponse
```internalStatus``` ```responseCode``` ```responseDescription``` ```merchantRequestID``` ```checkoutRequestID``` ```customerMessage```<br>
<br>

### C2B STK Push status (Check the status of a Lipa Na M-Pesa Online Payment.)
Use this API to check the status of a Lipa Na M-Pesa Online Payment.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.Environment;

MpesaRequestDto mpesaRequest = new MpesaRequestDto();
mpesaRequest.setBusinessShortCode("<REPLACE>");
mpesaRequest.setCheckoutRequestID("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.passKey("<REPLACE>")
.mpesaRequestDto(mpesaRequest)
.stkQuery();
```

#### MpesaResponse
```internalStatus``` ```responseCode``` ```responseDescription``` ```merchantRequestID``` ```checkoutRequestID``` ```resultCode``` ```resultDesc```<br>
<br>

### C2B STK Push Callback(Web-Hook) validation

```java

/**
 * Get this MpesaResponse from the CallbackURL web-hook.
 * Use MpesaResponse as the body object in the CallbackURL endpoint
 */
MpesaResponse mpesaResponse = new MpesaResponse();

new MpesaClient()
.responseParser(mpesaResponse,ResponseParserType.C2B_STK)

if(mpesaResponse.isInternalStatus()){
//stk push was successful, the customer successfully completed the payment
}else{
//an error occurred, customer didn't complete the payment
}
```

#### MpesaResponse
```internalStatus``` ```body```<br>
<br>

### C2B Register both validation and confirmation URL
Register URL API works hand in hand with Customer to Business (C2B) APIs and allows receiving payment notifications to your paybill. This API enables you to register the callback URLs via which you shall receive notifications for payments to your pay bill/till number. There are two URLs required for Register URL API: Validation URL and Confirmation URL.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
mpesaRequestDto.setBusinessShortCode("<REPLACE>");
mpesaRequestDto.setRegisterURLResponseType("<REPLACE>");
mpesaRequestDto.setConfirmationURL("<REPLACE>");
mpesaRequestDto.setValidationURL("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.mpesaRequestDto(mpesaRequestDto)
.C2BRegisterURL();
```

#### MpesaResponse
```originatorCoversationID``` ```responseCode``` ```responseDescription```<br>
<br>

### Validation/Confirmation Request Body
Consider using ```MpesaResponse``` to parse the request received from the URLS' you have registered

#### Payload
```transactionType``` ```transID``` ```transTime``` ```transAmount``` ```businessShortCode``` ```billRefNumber``` ```invoiceNumber``` ```orgAccountBalance``` ```thirdPartyTransID``` ```phoneNumber``` ```firstName``` ```middleName``` ```lastName```<br>
<br>

### B2B payment (Pay Bill & Buy Goods)
Enables bills payment from business account to a pay bill number or paybill store. You can use this API to pay on behalf of a consumer/requester. The transaction moves money from your MMF/Working account to the recipient’s utility account.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
mpesaRequestDto.setMpesaRequestType(MpesaRequestType.B2B_PAY_BILL); //MpesaRequestType.B2B_BUY_GOODS | MpesaRequestType.B2B_PAY_BILL
mpesaRequestDto.setAmount("<REPLACE>");
mpesaRequestDto.setPartyA("<REPLACE>");
mpesaRequestDto.setPartyB("<REPLACE>");
mpesaRequestDto.setAccountReference("<REPLACE>");
mpesaRequestDto.setPhoneNumber("<REPLACE>");
mpesaRequestDto.setRemarks("<REPLACE>");
mpesaRequestDto.setQueueTimeOutURL("<REPLACE>");
mpesaRequestDto.setResultURL("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.initiatorName("<REPLACE>")
.initiatorPassword("<REPLACE>")
.mpesaRequestDto(mpesaRequestDto)
.B2BPayment();
```

#### MpesaResponse
```internalStatus``` ```result```<br>
<br>

### B2B Result Body
Consider using ```MpesaResponse``` to parse the request received from the resultURL & queueTimeOutURL URLS' you had provided
Then call the method below
```java
MpesaResponse mpesaResponse;//Get this from your http post endpoint
        
new MpesaClient()
.responseParser(mpesaResponse,ResponseParserType.B2B_PAYMENT);
if(mpesaResponse.isInternalStatus()){
    //successful
}else{
   //failed 
}
```
#### Payload
```internalStatus```  ```result```<br>
<br>

### B2B Ussd Push (Pay Bill & Buy Goods)
Enables merchants to initiate USSD Push to Till enabling their fellow merchants to pay from their owned till numbers to the vendor's paybill.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
mpesaRequestDto.setSendingPartyShortCode("<REPLACE>");
mpesaRequestDto.setReceivingPartyShortCode("<REPLACE>");
mpesaRequestDto.setReceivingPartyName("<REPLACE>"); //AlphaNumeric
mpesaRequestDto.setAmt("<REPLACE>"); //Min 10
mpesaRequestDto.setPaymentRef("<REPLACE>"); //AlphaNumeric
mpesaRequestDto.setCallback("<REPLACE>");
mpesaRequestDto.setRequestRefId("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.mpesaRequestDto(mpesaRequestDto)
.B2BStk();
```

#### MpesaResponse
```internalStatus``` ```responseBody```<br>
<br>

### B2B Ussd Callback Response
Consider using ```MpesaResponse``` to parse the request received from the callback you had provided
Then call the method below
```java
MpesaResponse mpesaResponse;//Get this from your http post endpoint
        
new MpesaClient()
.responseParser(mpesaResponse,ResponseParserType.B2B_STK);
if(mpesaResponse.isInternalStatus()){
    //successful
}else{
   //failed 
}
```
#### Payload
```internalStatus``` ```resultCode``` ```resultDesc``` ```requestId``` ```amount``` ```paymentReference``` ```resultType``` ```conversationId``` ```transactionId``` ```status```<br>
<br>

### B2C Disbursement
used to make payments from a Business to Customers (Pay Outs), also known as Bulk Disbursements. B2C API is used in several scenarios by businesses that require to either make Salary Payments, Cashback payments, Promotional Payments(e.g. betting winning payouts), winnings, financial institutions withdrawal of funds, loan disbursements, etc.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
mpesaRequestDto.setOriginatorConversationId("<REPLACE>");
mpesaRequestDto.setB2CCommandID("<REPLACE>");
mpesaRequestDto.setAmount("<REPLACE>");
mpesaRequestDto.setBusinessShortCode("<REPLACE>");
mpesaRequestDto.setPhoneNumber("<REPLACE>");
mpesaRequestDto.setRemarks("<REPLACE>");
mpesaRequestDto.setQueueTimeOutURL("<REPLACE>");
mpesaRequestDto.setResultURL("<REPLACE>");
mpesaRequestDto.setOccassion("<REPLACE>");//Alpha-numeric (Max 100 chars)

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.initiatorName("<REPLACE>")
.initiatorPassword("<REPLACE>")
.mpesaRequestDto(mpesaRequestDto)
.B2CDisbursement();
```

#### MpesaResponse
```internalStatus``` ```responseCode``` ```originatorConversationId``` ```conversationId``` ```responseDescription```<br>
<br>

### B2C Disbursement Timeout & Result Response
Consider using ```MpesaResponse``` to parse the request received from the Timeout and Result URLS you had provided
Then call the method below
```java
MpesaResponse mpesaResponse;//Get this from your http post endpoint
        
new MpesaClient()
.responseParser(mpesaResponse,ResponseParserType.B2C);
if(mpesaResponse.isInternalStatus()){
    //successful
}else{
   //failed 
}
```
#### Payload
```internalStatus``` ```result```<br>
<br>

### Tax Remittance
Enable businesses to remit tax to Kenya Revenue Authority (KRA). To use this API, prior integration is required with KRA for tax declaration, payment registration number (PRN) generation, and exchange of other tax-related information.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
mpesaRequestDto.setTaxPRN("<REPLACE>");
mpesaRequestDto.setBusinessShortCode("<REPLACE>");
mpesaRequestDto.setAmount("<REPLACE>");
mpesaRequestDto.setRemarks("<REPLACE>");//max 100 chars
mpesaRequestDto.setQueueTimeOutURL("<REPLACE>");
mpesaRequestDto.setResultURL("<REPLACE>");

MpesaResponse mpesaResponse = new MpesaClient()
.environment("<REPLACE>")
.consumerSecret("<REPLACE>")
.consumerKey("<REPLACE>")
.initiatorName("<REPLACE>")
.initiatorPassword("<REPLACE>")
.mpesaRequestDto(mpesaRequestDto)
.remitTax();
```

#### MpesaResponse
```internalStatus``` ```responseCode``` ```originatorConversationId``` ```conversationId``` ```responseDescription```<br>
<br>

### Tax Remittance Timeout & Result Response
Consider using ```MpesaResponse``` to parse the request received from the Timeout and Result URLS you had provided
Then call the method below
```java
MpesaResponse mpesaResponse;//Get this from your http post endpoint
        
new MpesaClient()
.responseParser(mpesaResponse,ResponseParserType.TAX_REMITTANCE);
if(mpesaResponse.isInternalStatus()){
    //successful
}else{
   //failed 
}
```
#### Payload
```internalStatus``` ```result```<br>
<br>

### Dynamic QR Code
Generate a Dynamic QR which enables Safaricom M-PESA customers who have My Safaricom App or M-PESA app, to scan a QR (Quick Response) code, to capture till number and amount then authorize to pay for goods and services at select LIPA NA M-PESA (LNM) merchant outlets.

```java

import mpesa.dto.MpesaRequestDto;
import mpesa.util.*;

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
```

#### MpesaResponse
```internalStatus``` ```responseCode``` ```requestID``` ```responseDescription``` ```qrCode```<br>
<br>
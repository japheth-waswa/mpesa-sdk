package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mpesa.MpesaResponse;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Person{
    private long id;
    private String firstName;
    private String location;
}

class HelpersTest {

    private final String INITIATOR_PASSWORD = "Safaricom999!*!";

    @Test
    void jsonToList_String() throws JsonProcessingException {
        List<String> listData = Helpers.jsonToList(String.class,"[\"peter\",\"john\",\"caroline\"]");
        assertNotNull(listData);
        assertEquals(listData.get(1),"john");
    }

    @Test
    void jsonToList_POJO() throws JsonProcessingException {
        List<Person> listData = Helpers.jsonToList(Person.class,"[{\"id\":23,\"firstName\":\"jane\",\"location\":\"nairobi\"},{\"id\":45,\"firstName\":\"peter\",\"location\":\"mombasa\"},{\"id\":90,\"firstName\":\"alex\",\"location\":\"dubai\"}]");
        assertNotNull(listData);
        assertEquals(listData.get(1).getFirstName(),"peter");
        assertEquals(listData.get(2).getLocation(),"dubai");
    }

    @Test
    void jsonToList_POJO_throws() throws JsonProcessingException {
        assertThrows(JsonProcessingException.class,()->Helpers.jsonToList(Person.class,"[\"id\":23,\"firstName\":\"jane\",\"location\":\"nairobi\"},{\"id\":45,\"firstName\":\"peter\",\"location\":\"mombasa\"},{\"id\":90,\"firstName\":\"alex\",\"location\":\"dubai\"}]"));
    }

    @Test
    void jsonToPOJO_throws() {
        assertThrows(JsonProcessingException.class,()->Helpers.jsonToPOJO(String.class,"wowo"));
    }

    @Test
    void jsonToPOJO_POJO() throws JsonProcessingException {
        Person item = Helpers.jsonToPOJO(Person.class, """
                {"id":77,"firstName":"jeff","location":"nakuru"}
                """);
        assertEquals(item.getFirstName(),"jeff");
    }

    @Test
    void jsonToPOJO_POJO_stk_callback_fail() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Body":{"stkCallback":{"MerchantRequestID":"6e86-45dd-91ac-fd5d4178ab52485825","CheckoutRequestID":"ws_CO_20022024202003133719726698","ResultCode":1037,"ResultDesc":"DS timeout user cannot be reached"}}}
                """);
        System.out.println(mpesaResponse);
        assertEquals("ws_CO_20022024202003133719726698",mpesaResponse.getBody().getStkCallback().getCheckoutRequestID());
    }

    @Test
    void jsonToPOJO_POJO_stk_callback_success() throws JsonProcessingException {
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, """
                {"Body":{"stkCallback":{"MerchantRequestID":"29115-34620561-1","CheckoutRequestID":"ws_CO_191220191020363925","ResultCode":0,"ResultDesc":"The service request is processed successfully.","CallbackMetadata":{"Item":[{"Name":"Amount","Value":1},{"Name":"MpesaReceiptNumber","Value":"NLJ7RT61SV"},{"Name":"TransactionDate","Value":20191219102115},{"Name":"PhoneNumber","Value":254708374149}]}}}}
                """);
        System.out.println(mpesaResponse);
        assertEquals("ws_CO_191220191020363925",mpesaResponse.getBody().getStkCallback().getCheckoutRequestID());
        assertEquals("NLJ7RT61SV",mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().get(1).getValue());
        assertEquals("20191219102115",mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().get(2).getValue());
        assertEquals("PhoneNumber",mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().get(3).getName());
    }

    @Test
    void pojoToString() throws JsonProcessingException {
        String str  = Helpers.objToString(new Person(56,"jeff waswa","webuye"));
        assertEquals(str,"{\"id\":56,\"firstName\":\"jeff waswa\",\"location\":\"webuye\"}");
    }

    @Test
    void listToString() throws JsonProcessingException {
        String str  = Helpers.objToString(List.of(new Person(13,"john doe","kinshasa"),
                new Person(1004,"peter pan","singapore")));
        assertEquals(str,"[{\"id\":13,\"firstName\":\"john doe\",\"location\":\"kinshasa\"},{\"id\":1004,\"firstName\":\"peter pan\",\"location\":\"singapore\"}]");
    }

    @Test
    void generateBasicAuth() {
        String basicAuth  = Helpers.generateBasicAuth("f8a2if039fiasfdas","q039rikkdoo303");
        assertNotNull(basicAuth);
    }

    @Test
    void buildUrl() {
        URI uri  = Helpers.buildUrl(null,"http://example.com","url-test/crt","89238ff928dfadf");
        assertEquals("http://example.com/url-test/crt/89238ff928dfadf",uri.toString());
    }

    @Test
    void buildUrl_trailing_slash() {
        URI uri  = Helpers.buildUrl(null,"http://example.com/","pay-now/");
        assertEquals("http://example.com/pay-now",uri.toString());
    }

    @Test
    void buildUrl_with_query_params_without_spaces() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("filter","name");
        queryParams.put("sort","asc");
        URI uri  = Helpers.buildUrl(queryParams,"http://example.com/","pay-now/");
        assertEquals("http://example.com/pay-now?filter=name&sort=asc",uri.toString());
    }
    @Test
    void buildUrl_with_query_params_with_spaces() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("filter","name");
        queryParams.put("sort","asc");
        queryParams.put("searchTerm","mindful cat");
        URI uri  = Helpers.buildUrl(queryParams,"http://example.com/","pay-now/");
        System.out.println(uri);
        assertEquals("http://example.com/pay-now?filter=name&searchTerm=mindful+cat&sort=asc",uri.toString());
    }

    @Test
    void removeTrailingSlash_001() {
        String str  =  Helpers.removeTrailingSlash("http://example.com/");
        assertEquals("http://example.com",str);
    }

    @Test
    void removeTrailingSlash_002() {
        String str  =  Helpers.removeTrailingSlash("http://example.com/fruits");
        assertEquals("http://example.com/fruits",str);
    }

    @Test
    void bindBearerToken() {
        String bearerToken  = Helpers.bindBearerToken("f9fk2092fik22pf003ifjlj2289ufahn27faj");
        assertEquals("Bearer f9fk2092fik22pf003ifjlj2289ufahn27faj",bearerToken);
    }

    @Test
    void formatDateTime_now() {
        String str = Helpers.formatDateTime("yyyyMMddHHmmss");
        assertNotNull(str);
    }

    @Test
    void FormatDateTime_custom() {
        String str = Helpers.formatDateTime(LocalDateTime.parse("2020-09-12 14:09:23", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),"yyyyMMddHHmmss");
        assertEquals("20200912140923",str);
    }

    @Test
    void generateSecurityCredentials() throws NoSuchPaddingException, IllegalBlockSizeException, CertificateException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String securityCredentials  = Helpers.generateSecurityCredentials(INITIATOR_PASSWORD,Helpers.MPESA_CERT_DEV);
        System.out.println(securityCredentials);
        assertNotNull(securityCredentials);
    }
}
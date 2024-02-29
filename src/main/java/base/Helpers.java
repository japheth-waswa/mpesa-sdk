package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Generic helper class for common functions
 */
public class Helpers {

    /**
     * default mpesa timestamp format
     */
    public static final String MPESA_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    /**
     * develpment mpesa certificate
     */
    public static final String MPESA_CERT_DEV = "mpesa/SandboxCertificate.cer";

    /**
     * production mpesa certificate
     */
    public static final String MPESA_CERT_PROD = "mpesa/ProductionCertificate.cer";

    /**
     * Not allowed to instantiate this class
     */
    private Helpers() {
    }

    /**
     * Parse json string to list
     * @param elementType Dto object class
     * @param json String representation of the json
     * @return List of type T
     * @param <T> Object to be returned
     * @throws JsonProcessingException While parsing the json
     */
    public static <T> List<T> jsonToList(Class<T> elementType, String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return objectMapper.readValue(json, typeFactory.constructCollectionType(List.class, elementType));
    }

    /**
     * Parse json string to POJO
     * @param elementType POJO object class
     * @param json String representation of the json
     * @return POJO of type T
     * @param <T> Object of type T
     * @throws JsonProcessingException While parsing the json
     */
    public static <T> T jsonToPOJO(Class<T> elementType, String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, elementType);
    }

    /**
     * Convert a POJO to string
     * @param data POJO
     * @return String from the POJO
     * @param <T> POJO class
     * @throws JsonProcessingException While processing
     */
    public static <T> String objToString(T data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper().writeValueAsString(data);
    }

    /**
     * Convert a List of POJO to string
     * @param data List of POJOs
     * @return String from the List of POJOs
     * @param <T> POJO class
     * @throws JsonProcessingException While processing
     */
    public static <T> String objToString(List<T> data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper().writeValueAsString(data);
    }

    /**
     * Remove any trailing slashes from a string
     * @param input String
     * @return Parsed string without trailing slashes
     */
    public static String removeTrailingSlash(String input) {
        return input.replaceAll("/\\z", "");
    }

    /**
     * Build URL from the provided params
     * @param queryParams Query parameters
     * @param urlParts Parts of the url
     * @return URI
     */
    public static URI buildUrl(Map<String, String> queryParams, String... urlParts) {
        StringJoiner urlJoiner = new StringJoiner("/");
        for (String part : urlParts) {
            urlJoiner.add(removeTrailingSlash(part));
        }
        String baseUrl = urlJoiner.toString();
        if (queryParams != null && !queryParams.isEmpty()) {
            StringJoiner queryJoiner = new StringJoiner("&");
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                queryJoiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) +
                        "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            baseUrl += "?" + queryJoiner;
        }
        return URI.create(baseUrl);
    }

    /**
     * Generates basic auth string
     * @param username auth username
     * @param password auth password
     * @return basic auth
     */
    public static String generateBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Binds the bearer token
     * @param accessToken The access token
     * @return bearer token
     */
    public static String bindBearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    /**
     * Formats the date provided the pattern
     * @param dateTime Instance of LocalDateTime
     * @param pattern Date string pattern
     * @return formatted date as string
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Get the current LocalDateTime with a certain format
     * @param pattern ate string pattern ie yyyy-MMM-DD
     * @return formatted date as string
     */
    public static String formatDateTime(String pattern) {
        return formatDateTime(LocalDateTime.now(), pattern);
    }

    /**
     * Algorithm to generate mpesa security credentials
     * @param password  mpesa password
     * @param certificatePath development or production certificate
     * @throws IOException While processing
     * @throws CertificateException While processing
     * @throws NoSuchPaddingException While processing
     * @throws NoSuchAlgorithmException While processing
     * @throws InvalidKeyException While processing
     * @throws IllegalBlockSizeException While processing
     * @throws BadPaddingException While processing
     * @return String
     */
    public static String generateSecurityCredentials(String password, String certificatePath) throws IOException, CertificateException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Load certificate
        InputStream certificateInputStream = Helpers.class.getClassLoader().getResourceAsStream(certificatePath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);

        if (certificateInputStream != null) {
            certificateInputStream.close();
        }

        // Create Cipher instance
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());

        // Encrypt password
        byte[] encryptedPassword = cipher.doFinal(password.getBytes());

        // Base64 encode the encrypted password
        return Base64.getEncoder().encodeToString(encryptedPassword);
    }
}

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

public class Helpers {
    public static final String MPESA_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    //    public static final String MPESA_CERT_DEV = "resources/mpesa/SandboxCertificate.cer";
    public static final String MPESA_CERT_DEV = "mpesa/SandboxCertificate.cer";
    public static final String MPESA_CERT_PROD = "mpesa/ProductionCertificate.cer";

    public static <T> List<T> jsonToList(Class<T> elementType, String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return objectMapper.readValue(json, typeFactory.constructCollectionType(List.class, elementType));
    }

    public static <T> T jsonToPOJO(Class<T> elementType, String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(json, elementType);
    }

    public static <T> String objToString(T data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper().writeValueAsString(data);
    }

    public static <T> String objToString(List<T> data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper().writeValueAsString(data);
    }

    public static String removeTrailingSlash(String input) {
        return input.replaceAll("/\\z", "");
    }

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

    public static String generateBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public static String bindBearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatDateTime(String pattern) {
        return formatDateTime(LocalDateTime.now(), pattern);
    }

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

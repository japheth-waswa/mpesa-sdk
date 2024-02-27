package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpRequest.newBuilder;

//@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApiClient<T extends Request, S extends Response> implements AutoCloseable {
    private final String POJO_EMPTY_JSON_RES = """
            {"message":"no content"}
            """;
    private final String LIST_EMPTY_JSON_RES= """
            [{"message":"no content"}]
            """;
    private String authorization;
    private List<Header> headers;
    private String baseUri;
    private T reqClass;
    private Class<S> responseClass;
    private ObjectMapper objectMapper;
    private TypeFactory typeFactory;
    private HttpClient httpClient;

    public ApiClient() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        typeFactory = objectMapper.getTypeFactory();
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    private void appendHeaders(HttpRequest.Builder builder) {
        if (headers != null && !headers.isEmpty()) {
            for (Header header : headers) {
                builder.header(header.headerName(), header.headerValue());
            }
        }
    }

    private HttpRequest createGetRequest(String uri) {
        var builder = newBuilder().uri(URI.create(uri));
        appendHeaders(builder);
        return builder.build();
    }

    private HttpRequest createPostRequest(String uri, String requestBody) {
        var builder = newBuilder()
                .uri(URI.create(uri));
        builder.header("Content-Type", ContentType.JSON.getValue());
        appendHeaders(builder);
        return builder.POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    private List<S> parseResponseList(String responseBody) throws JsonProcessingException {
//        return objectMapper.readValue(responseBody, typeFactory.constructCollectionType(List.class, responseClass));
        return objectMapper.readValue(responseBody == null || responseBody.isEmpty()? LIST_EMPTY_JSON_RES :responseBody, typeFactory.constructCollectionType(List.class, responseClass));
    }

    private S parseResponse(String responseBody) throws JsonProcessingException {
//        return objectMapper.readValue(responseBody, responseClass);
        return objectMapper.readValue(responseBody == null || responseBody.isEmpty()? POJO_EMPTY_JSON_RES :responseBody, responseClass);
    }

    private void handleAsyncResponse(HttpResponse<String> httpResponse, boolean isListRes, Callback<S> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                if (isListRes) {
                    callback.onResponse(parseResponseList(httpResponse.body()));
                } else {
                    callback.onResponse(parseResponse(httpResponse.body()));
                }
            } catch (JsonProcessingException e) {
                callback.onError(e);
            }
        }).exceptionally(throwable -> {
            callback.onError(throwable);
            return null;
        });
    }

    private HttpResponse<String> makeRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<String> httpResponse;
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return httpResponse;
    }

    private void makeRequest(HttpRequest httpRequest, boolean isListRes, Callback<S> callback) {
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenAcceptAsync(response -> handleAsyncResponse(response, isListRes, callback))
                .exceptionally(throwable -> {
                    callback.onError(throwable);
                    return null;
                });
    }

    public S get() throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = makeRequest(createGetRequest(baseUri));
        try {
            return parseResponse(httpResponse.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<S> get(boolean isListRes) throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = makeRequest(createGetRequest(baseUri));
        return parseResponseList(httpResponse.body());

    }

    public void get(boolean isListRes, Callback<S> callback) {
        makeRequest(createGetRequest(baseUri), isListRes, callback);
    }

    public S post() throws IOException, InterruptedException {
        HttpRequest httpRequest = createPostRequest(baseUri, reqClass.getPostBody());
        HttpResponse<String> httpResponse = makeRequest(httpRequest);
        return parseResponse(httpResponse.body());
    }

    public List<S> post(boolean isListRes) throws IOException, InterruptedException {
        HttpRequest httpRequest = createPostRequest(baseUri, reqClass.getPostBody());
        HttpResponse<String> httpResponse = makeRequest(httpRequest);
        return parseResponseList(httpResponse.body());
    }

    public void post(boolean isListRes, Callback<S> callback) {
        makeRequest(createPostRequest(baseUri, reqClass.getPostBody()), isListRes, callback);
    }


}

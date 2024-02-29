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

/**
 * Generic api client for basic http methods
 * @param <T> The request class
 * @param <S> The resposonse class
 */
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

    /**
     * No Args constructor
     */
    public ApiClient() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        typeFactory = objectMapper.getTypeFactory();
        httpClient = HttpClient.newHttpClient();
    }

    /**
     * Closes this resource and performs garbage collection
     * @throws Exception When closing the resource
     */
    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * Add headers to the request
     * @param builder HttpRequest builder instance
     */
    private void appendHeaders(HttpRequest.Builder builder) {
        if (headers != null && !headers.isEmpty()) {
            for (Header header : headers) {
                builder.header(header.headerName(), header.headerValue());
            }
        }
    }

    /**
     * Builds the get request
     * @param uri Resources URI
     * @return Returns HttpRequest instance
     */
    private HttpRequest createGetRequest(String uri) {
        var builder = newBuilder().uri(URI.create(uri));
        appendHeaders(builder);
        return builder.build();
    }


    /**
     * Builds the post request
     * @param uri Resources URI
     * @param requestBody Body of the request in json format
     * @return HttpRequest instance
     */
    private HttpRequest createPostRequest(String uri, String requestBody) {
        var builder = newBuilder()
                .uri(URI.create(uri));
        builder.header("Content-Type", ContentType.JSON.getValue());
        appendHeaders(builder);
        return builder.POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    /**
     * Parses the response body to list of objects
     * @param responseBody Json response body as String instance
     * @return List of objects specified as S
     * @throws JsonProcessingException When parsing of the json response fails
     */
    private List<S> parseResponseList(String responseBody) throws JsonProcessingException {
        return objectMapper.readValue(responseBody == null || responseBody.isEmpty()? LIST_EMPTY_JSON_RES :responseBody, typeFactory.constructCollectionType(List.class, responseClass));
    }

    /**
     * Parses the response body to a single object
     * @param responseBody
     * @return Single object instance
     * @throws JsonProcessingException
     */
    private S parseResponse(String responseBody) throws JsonProcessingException {
//        return objectMapper.readValue(responseBody, responseClass);
        return objectMapper.readValue(responseBody == null || responseBody.isEmpty()? POJO_EMPTY_JSON_RES :responseBody, responseClass);
    }

    /**
     * Handles the async response
     * @param httpResponse The response as a json string
     * @param isListRes Whether the response is a list
     * @param callback Implementation that handles the response as an async operation
     */
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

    /**
     * Makes the actual request
     * @param httpRequest
     * @return HttpResponse as String
     * @throws IOException When making the request
     * @throws InterruptedException When making the request
     */
    private HttpResponse<String> makeRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<String> httpResponse;
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return httpResponse;
    }

    /**
     * Make async request
     * @param httpRequest The request
     * @param isListRes Whether the response is a list
     * @param callback Implementation to handle async request
     */
    private void makeRequest(HttpRequest httpRequest, boolean isListRes, Callback<S> callback) {
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenAcceptAsync(response -> handleAsyncResponse(response, isListRes, callback))
                .exceptionally(throwable -> {
                    callback.onError(throwable);
                    return null;
                });
    }

    /**
     * Get request that returns a single object
     * @return Single object of type S
     * @throws IOException During request execution
     * @throws InterruptedException During request execution
     */
    public S get() throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = makeRequest(createGetRequest(baseUri));
        try {
            return parseResponse(httpResponse.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get request that returns a list of S objects
     * @param isListRes Whether list or object
     * @return List of S objects
     * @throws IOException During request execution
     * @throws InterruptedException During request execution
     */
    public List<S> get(boolean isListRes) throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = makeRequest(createGetRequest(baseUri));
        return parseResponseList(httpResponse.body());

    }

    /**
     * Async get request
     * @param isListRes Whether list or single object
     * @param callback Implementation to handle async request
     */
    public void get(boolean isListRes, Callback<S> callback) {
        makeRequest(createGetRequest(baseUri), isListRes, callback);
    }

    /**
     * Post request that returns single object of type S
     * @return Single S object
     * @throws IOException During request execution
     * @throws InterruptedException During request execution
     */
    public S post() throws IOException, InterruptedException {
        HttpRequest httpRequest = createPostRequest(baseUri, reqClass.getPostBody());
        HttpResponse<String> httpResponse = makeRequest(httpRequest);
        return parseResponse(httpResponse.body());
    }

    /**
     * Post request that returns list of objects of type S
     * @param isListRes Whether list or single object
     * @return List of S objects
     * @throws IOException During request execution
     * @throws InterruptedException During request execution
     */
    public List<S> post(boolean isListRes) throws IOException, InterruptedException {
        HttpRequest httpRequest = createPostRequest(baseUri, reqClass.getPostBody());
        HttpResponse<String> httpResponse = makeRequest(httpRequest);
        return parseResponseList(httpResponse.body());
    }

    /**
     * Async post request
     * @param isListRes Whether list or single object
     * @param callback  Implementation to handle async request
     */
    public void post(boolean isListRes, Callback<S> callback) {
        makeRequest(createPostRequest(baseUri, reqClass.getPostBody()), isListRes, callback);
    }


}

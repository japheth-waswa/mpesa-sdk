package base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class TestListResponse implements Response {
    @JsonProperty("_id")
    private String id;
    private String source;
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updated;
    private boolean used;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class TestJsonResponse implements Response {
    private String error;
}

@Data
class TestReqPost implements Request {
    @Override
    public String getPostBody() {
        return "{\"serviceId\":\"63ebef9a68fda056bd5935cd\"}";
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class TestResPost implements Response {
    private String message;
}

class TestListCallback implements Callback<TestListResponse> {
    @Override
    public void onResponse(List<TestListResponse> list) {
        System.out.println("^".repeat(50));
        System.out.println(list);
        System.out.println("^".repeat(50));
    }

    @Override
    public void onError(Throwable exception) {
        System.out.println(exception.getMessage());
    }
}


//@ExtendWith(MockitoExtension.class)
class ApiClientTest {
    private final String CATS_API = "https://cat-fact.herokuapp.com/facts";
    private final String ZURIMATE_FILE_API = "https://file.zurimate.com";
    private final String ZURIMATE_TRANSACTION = "https://api.zurimate.com/v1/transaction";

    @Test
    void get_POJO() throws Exception {

        try (ApiClient<Request, TestJsonResponse> apiClient = new ApiClient<>();
             ApiClient<Request, TestJsonResponse> apiClientBuild = apiClient.toBuilder()
                     .responseClass(TestJsonResponse.class)
                     .baseUri(ZURIMATE_FILE_API)
                     .build()) {
            TestJsonResponse res = apiClientBuild.get();
            System.out.println(res);
            assertNotNull(res);
        }

    }

    @Test
    void get_POJO_Async() throws Exception {
        try (ApiClient<Request, TestJsonResponse> apiClient = new ApiClient<>();
             ApiClient<Request, TestJsonResponse> apiClientBuild = apiClient.toBuilder()
                     .responseClass(TestJsonResponse.class)
                     .baseUri(ZURIMATE_FILE_API)
                     .build()) {
            apiClientBuild.get(false, new Callback<TestJsonResponse>() {
                @Override
                public void onResponse(TestJsonResponse obj) {
                    System.out.println("*".repeat(50));
                    System.out.println(obj);
                    System.out.println("*".repeat(50));
                }

                @Override
                public void onError(Throwable exception) {
                    System.out.println(exception.getMessage());
                }
            });
        }

        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    void get_List() throws Exception {

        try (ApiClient<Request, TestListResponse> apiClient = new ApiClient<>();
             ApiClient<Request, TestListResponse> apiClientBuild = apiClient.toBuilder()
                     .responseClass(TestListResponse.class)
                     .baseUri(CATS_API)
                     .build()) {
            List<TestListResponse> res = apiClientBuild.get(true);
            System.out.println(res);
            for (TestListResponse testResponse : res) {
                System.out.println(testResponse);
            }
            assertNotNull(res);
        }

    }


    @Test
    void get_List_Async() throws Exception {

        try (ApiClient<Request, TestListResponse> apiClient = new ApiClient<>();
             ApiClient<Request, TestListResponse> apiClientBuild = apiClient.toBuilder()
                     .responseClass(TestListResponse.class)
                     .baseUri(CATS_API)
                     .build();) {
            apiClientBuild.get(true, new TestListCallback());
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void post_POJORes() throws Exception {
        TestReqPost testReqPost = new TestReqPost();
        try (ApiClient<TestReqPost, TestResPost> apiClient = new ApiClient<>();
             ApiClient<TestReqPost, TestResPost> apiClientBuild = apiClient.toBuilder()
                     .reqClass(testReqPost)
                     .responseClass(TestResPost.class)
                     .baseUri(ZURIMATE_TRANSACTION)
                     .build()) {
            TestResPost res = apiClientBuild.post();

            System.out.println(res);
            assertNotNull(res);
            assertEquals(res.getMessage(), "Request successful");
        }

    }

    @Test
    void post_POJORes_Async() throws Exception {
        TestReqPost testReqPost = new TestReqPost();
        try (ApiClient<TestReqPost, TestResPost> apiClient = new ApiClient<>();
             ApiClient<TestReqPost, TestResPost> apiClientBuild = apiClient.toBuilder()
                     .reqClass(testReqPost)
                     .responseClass(TestResPost.class)
                     .baseUri(ZURIMATE_TRANSACTION)
                     .build()) {

            apiClientBuild.post(false, new Callback<TestResPost>() {
                @Override
                public void onResponse(TestResPost obj) {
                    System.out.println("%".repeat(50));
                    System.out.println(obj);
                    System.out.println("%".repeat(50));
                }

                @Override
                public void onError(Throwable exception) {
                    System.out.println(exception.getMessage());
                }
            });
        }
        TimeUnit.SECONDS.sleep(10);
    }
}
package base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * ApiClient Request instance
 */
public interface Request {

    /**
     * Parses an object to JSON string
     * @return String
     */
    default String getPostBody() {

        @Getter
        @Setter
        @AllArgsConstructor
        class GenericPOJO{
            private long id;
            private String name;
        }

        try {
            return new ObjectMapper().writeValueAsString(new GenericPOJO(90,"nairobi"));
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}

package base;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Request content-type
 */
@AllArgsConstructor
@Getter
public enum ContentType {
    JSON("application/json");
    
    private String value;
}

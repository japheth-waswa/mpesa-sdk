package base;

import java.util.List;

/**
 * Implement this class to process async request
 * @param <T> Object type
 */
public interface Callback<T> {

     /**
      * POJO data
      * @param obj Received POJO
      */
     default void onResponse(T obj) {}

     /**
      * List of POJOs
      * @param list List of POJOs
      */
     default void onResponse( List<T> list) {}

     /**
      * When error occurs
      * @param exception When error occurs
      */
     void onError(Throwable exception);
}

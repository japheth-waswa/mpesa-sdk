package base;

import java.util.List;

public interface Callback<T> {
     default void onResponse(T obj) {}
     default void onResponse( List<T> list) {}
     void onError(Throwable exception);
}
//public interface Callback {
//     default <T> void onResponse(T obj) {}
//     default <T> void onResponse( List<T> list) {}
//     void onError(Throwable exception);
//}

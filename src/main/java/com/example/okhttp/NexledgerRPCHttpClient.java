package com.example.okhttp;

import com.example.okhttp.model.ConnectionPoolOption;
import com.example.okhttp.model.EndPoint;
import com.example.okhttp.model.Timeout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;


/**
 * OkHttpClient Util Class
 *
 * @author: shyoung.kim@samsung.com
 *
 */
public class NexledgerRPCHttpClient<T> implements HttpClient<T> {
    private OkHttpClient okHttpClient;
    private EndPoint endPoint;
    private EndPoint proxy;

    private Gson gson;
    private Gson gsonPretty;
    public static MediaType DEFAULT_MEDIA_TYPE = MediaType.parse("application/json");

    @Getter
    public enum HttpProtocol {
        HTTP("http"),
        HTTPS("https");

        private final String value;

        static private final Map<String, HttpProtocol> CACHE_MAP;

        static {
            CACHE_MAP = new HashMap<>();
            Arrays.stream(HttpProtocol.values()).forEach(v -> CACHE_MAP.put(v.getValue(), v));
        }

        HttpProtocol(String value) {
            this.value = value;
        }

        public static HttpProtocol of(String value) {
            if (CACHE_MAP.containsKey(value)) {
                return CACHE_MAP.get(value);
            }

            return HTTP;
        }
    }

    @Getter
    public enum Method {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        private final String method;

        Method(String method) {
            this.method = method;
        }
    }

    public NexledgerRPCHttpClient() {
        try {
            this.init(new EndPoint(), null,
                    Timeout.DEFAULT_READ_TIMEOUT, Timeout.DEFAULT_WRITE_TIMEOUT, Timeout.DEFAULT_CONNECT_TIMEOUT,
                    null, new Gson());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param endPoint endPoint
     * @param readTimout readTimeout (MilliSeconds)
     * @param writeTimeout writeTimeout (MilliSeconds)
     * @param connectTimeout connectTimeout (MilliSeconds)
     */
    public NexledgerRPCHttpClient(EndPoint endPoint, int readTimout, int writeTimeout, int connectTimeout) {
        try {
            this.init(endPoint, null,
                    readTimout, writeTimeout, connectTimeout, 
                    null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param endPoint endPoint (Host, Port, UserName, Password)
     * @param timeout timeout option (read, write, connect)
     */
    public NexledgerRPCHttpClient(EndPoint endPoint, Timeout timeout) {
        this.init(endPoint, null,
                timeout.getReadTimeout(), timeout.getWriteTimeout(), timeout.getConnectTimeout(),
                null, null);
    }

    /**
     *
     * @param endPoint endPoint (Host, Port, UserName, Password)
     * @param proxy proxy (Host, Port, UserName, Password)
     * @param timeout timeout option (read, write, connect)
     */
    public NexledgerRPCHttpClient(EndPoint endPoint, EndPoint proxy, Timeout timeout) {
        try {
            this.init(endPoint, proxy, timeout.getReadTimeout(), timeout.getWriteTimeout(),
                    timeout.getConnectTimeout(), null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param endPoint endPoint (Host, Port, UserName, Password)
     * @param proxy proxy (Host, Port, UserName, Password)
     * @param readTimeout timeout option (read)
     * @param writeTimeout timeout option (write)
     * @param connectionTimeout timeout option (connect)
     */
    public NexledgerRPCHttpClient(EndPoint endPoint, EndPoint proxy, int readTimeout,
                                  int writeTimeout, int connectionTimeout) {
        try {
            this.init(endPoint,  proxy, readTimeout, writeTimeout, connectionTimeout, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param endPoint endPoint (Host, Port, UserName, Password)
     * @param proxy proxy (Host, Port, UserName, Password)
     * @param readTimeout timeout option (read, MilliSeconds)
     * @param writeTimeout timeout option (write, MilliSeconds)
     * @param connectTimeout timeout option (connect, MilliSeconds)
     * @param connectionPoolOption maxConnectionCount, keepAliveDuration(MilliSeconds), retryCount
     * @param gson com.google.gson.Gson
     */
    private void init(EndPoint endPoint, EndPoint proxy,
                      int readTimeout, int writeTimeout, int connectTimeout,
                      ConnectionPoolOption connectionPoolOption, Gson gson) {
        try {
            if (gson != null) {
                this.gson = gson;
                this.gsonPretty = gson.newBuilder().setPrettyPrinting().create();
            } else {
                this.gson = new Gson();
                this.gsonPretty = new GsonBuilder().setPrettyPrinting().create();
            }

            Builder builder = new Builder()
                    .readTimeout(readTimeout, Timeout.DEFAULT_TIME_UNIT)
                    .writeTimeout(writeTimeout, Timeout.DEFAULT_TIME_UNIT)
                    .connectTimeout(connectTimeout, Timeout.DEFAULT_TIME_UNIT);

            if (connectionPoolOption != null) {
                builder.connectionPool(new ConnectionPool(
                        connectionPoolOption.getMaxConnectionCount(),
                        connectionPoolOption.getKeepAliveDuration(),
                        connectionPoolOption.getTimeUnit())
                );
            } else {
                builder.connectionPool(
                        new ConnectionPool(
                                ConnectionPoolOption.DEFAULT_MAX_CONNECTION_COUNT,
                                ConnectionPoolOption.DEFAULT_KEEP_ALIVE_DURATION,
                                ConnectionPoolOption.DEFAULT_TIME_UNIT)
                );
            }

            if (proxy != null) {
                this.proxy = proxy;
                Proxy javaNetProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
                builder.proxy(javaNetProxy);
            }

            this.okHttpClient = builder.build();

            this.endPoint = endPoint;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestApiUrl(String path) {
        try {
            String url = this.endPoint.getUrl() + path;
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestApiUrl(String path, Object object) {
        try {
            HttpUrl.Builder httpUrlBuilder
                    = Objects.requireNonNull(
                    HttpUrl.parse(this.getRestApiUrl(path))
            ).newBuilder();

            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(object) != null) {
                    httpUrlBuilder.addQueryParameter(field.getName(), field.get(object).toString());
                }
                field.setAccessible(false);
            }

            URL url = httpUrlBuilder.build().url();
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestApiUrl(String path, Map<String, String> queryParams) {
        try {
            HttpUrl.Builder httpUrlBuilder
                    = Objects.requireNonNull(
                    HttpUrl.parse(this.getRestApiUrl(path))
            ).newBuilder();

            queryParams.forEach(httpUrlBuilder::addQueryParameter);

            URL url = httpUrlBuilder.build().url();
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private T parseGson(String jsonString, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            if (parameterClass.length > 0) {
                result = gson.fromJson(jsonString, TypeToken.getParameterized(returnClass, parameterClass).getType());
            }
            else {
                result = gson.fromJson(jsonString, returnClass);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    // TODO: Handle Response. (if not successful response)
    // response.isSuccessful();
    // response.isRedirect();
    @Override
    public String callMethod(String httpMethod, String path) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // RequestBody Builder
                    requestBuilder.method(
                            httpMethod,
                            RequestBody.create(new byte[0], DEFAULT_MEDIA_TYPE)
                    );
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithJson(String httpMethod, String path, Object jsonObject) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder
                            .url(this.getRestApiUrl(path, jsonObject))
                            .get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // RequestBody Builder
                    RequestBody requestBody = RequestBody.create(
                            gson.toJson(jsonObject, jsonObject.getClass()),
                            MediaType.parse("application/json")
                    );

                    requestBuilder.method(httpMethod, requestBody);
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithFormData(String httpMethod, String path, Map<String, String> parameterMap) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder.url(this.getRestApiUrl(path, parameterMap))
                            .get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // FormBody.Builder (RequestBodyBuilder)
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    parameterMap.forEach(formBodyBuilder::add);

                    RequestBody requestBody = formBodyBuilder.build();
                    requestBuilder.method(httpMethod, requestBody);
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> parameterMap, List<File> fileList) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder.url(this.getRestApiUrl(path, parameterMap))
                            .get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // RequestBodyBuilder
                    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM);

                    for (File file : fileList) {
                        requestBodyBuilder.addFormDataPart(
                                "file",
                                file.getName(),
                                RequestBody.create(file, MediaType.parse("text/plain"))
                        );
                    }

                    for (String key : parameterMap.keySet()) {
                        requestBodyBuilder.addFormDataPart(key, parameterMap.get(key));
                    }

                    RequestBody requestBody = requestBodyBuilder.build();
                    requestBuilder.method(httpMethod, requestBody);
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethod(String httpMethod, String path, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethod(httpMethod, path);

            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethodWithJson(String httpMethod, String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithJson(httpMethod, path, jsonObject);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethodWithFormData(String httpMethod, String path, Map<String, String> parameterMap, Class<T> returnClass, Class<?> ...parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithFormData(httpMethod, path, parameterMap);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public T callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> parameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithMultipartFormData(httpMethod, path, parameterMap, fileList);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callGetMethod(String path) {
        return this.callMethod("GET", path);
    }

    @Override
    public String callGetMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("GET", path, pathParameterJsonObject);
    }

    @Override
    public String callGetMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("GET", path, pathParameterMap);
    }

    @Override
    public T callGetMethod(String path, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethod("GET", path, returnClass, parameterClass);
    }

    @Override
    public T callGetMethodWithJson(String path, Object pathParameterJsonObject, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithJson("GET", path, pathParameterJsonObject, returnClass, parameterClass);
    }

    @Override
    public T callGetMethodWithFormData(String path, Map<String, String> pathParameterMap, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithFormData("GET", path, pathParameterMap, returnClass, parameterClass);
    }

    @Override
    public String callPostMethod(String path) {
        return this.callMethod("POST", path);
    }

    @Override
    public String callPostMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("POST", path, pathParameterJsonObject);
    }

    @Override
    public String callPostMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("POST", path, pathParameterMap);
    }

    @Override
    public String callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("POST", path, formParameterMap, fileList);
    }

    @Override
    public T callPostMethod(String path, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callGetMethodWithJson("POST", path, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithJson("POST", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithFormData("POST", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?>... parameterClass) {
        return callMethodWithMultipartFormData("POST", path, formParameterMap, fileList, returnClass, parameterClass);
    }

    @Override
    public String callPutMethod(String path) {
        return this.callMethod("PUT", path);
    }

    @Override
    public String callPutMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("PUT", path, pathParameterJsonObject);
    }

    @Override
    public String callPutMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("PUT", path, pathParameterMap);
    }

    @Override
    public String callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("PUT", path, formParameterMap, fileList);
    }

    @Override
    public T callPutMethod(String path, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethod("PUT", path, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithJson("PUT", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithFormData("PUT", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass) {
        return this.callMethodWithMultipartFormData("PUT", path, formParameterMap, fileList, returnClass, parameterClass);
    }

    @Override
    public String callDeleteMethod(String path) {
        return this.callMethod("DELETE", path);
    }

    @Override
    public String callDeleteMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("DELETE", path, pathParameterJsonObject);
    }

    @Override
    public String callDeleteMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("DELETE", path, pathParameterMap);
    }

    @Override
    public String callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("DELETE", path, formParameterMap, fileList);
    }

    @Override
    public T callDeleteMethod(String path, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethod("DELETE", path, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithJson("DELETE", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithFormData("DELETE", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithMultipartFormData("DELETE", path, formParameterMap, fileList, returnClass, parameterClass);
    }

}
